/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.tools.gradle.testing;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.fs.DirectoryName;
import io.spine.tools.gradle.SourceSetName;
import io.spine.tools.gradle.task.TaskName;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.io.Copy.copyDir;
import static io.spine.tools.gradle.SourceSetName.main;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;

/**
 * Allows to configure a Gradle project for testing needs.
 *
 * <p>The project operates in the given test project directory and allows executing Gradle tasks.
 */
@SuppressWarnings("unused") /* Some methods are used only in downstream repositories,
    e.g. `mc-java`. This suppression should be removed after the split of modules from the `base`
    is finished, and we have Model Compiler and related artifacts originated from `base` settled
    in their new repositories. */
public final class GradleProject {

    private static final String buildSrcDir = "buildSrc";

    private final String name;

    private final GradleRunner runner;
    private final RunnerArguments arguments;

    /**
     * Creates new builder for the project.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Obtains the name of the Java Gradle plugin.
     */
    public static String javaPlugin() {
        return "java";
    }

    private GradleProject(Builder builder) throws IOException {
        this.name = builder.origin;
        this.arguments = builder.arguments;
        this.runner = GradleRunner.create()
                .withProjectDir(builder.dir)
                .withDebug(builder.debug);
        if (builder.addPluginUnderTestClasspath) {
            runner.withPluginClasspath();
        }
        if (builder.environment != null) {
            runner.withEnvironment(builder.environment);
        }
        writeGradleScripts();
        writeBuildSrc();
        writeProtoFiles(builder.protoFileNames);
        writeJavaFiles(builder.javaFileNames);
    }

    private void writeGradleScripts() throws IOException {
        Path projectDir = projectDir();
        BuildGradle buildGradle = new BuildGradle(projectDir);
        buildGradle.createFile();

        TestEnvGradle testEnvGradle = new TestEnvGradle(projectDir);
        testEnvGradle.createFile();
    }

    /** The directory of this project. */
    private Path projectDir() {
        return runner.getProjectDir()
                     .toPath();
    }

    private void writeBuildSrc() throws IOException {
        Path projectRoot = RootProject.path();
        Path buildSrc = projectRoot.resolve(buildSrcDir);
        Path target = projectDir();
        copyDir(buildSrc, target, new SkipNonSrcDirs());
    }

    /**
     * The predicate to prevent copying unnecessary files when {@linkplain #writeBuildSrc() copying}
     * the {@code buildSrc} directory.
     *
     * <p>The predicate 1) saves on unnecessary copying, 2) prevents file locking issue
     * under Windows, which fails the build because locked under the {@code .gradle}
     * directory could not be copied.
     */

    private static class SkipNonSrcDirs implements Predicate<Path> {

        @Override
        public boolean test(Path path) {
            String str = path.toString();
            String slash = File.separator;
            // Use leading slash to accept `.gradle` files, but filter out the Gradle cache dir.
            boolean isGradleCache = str.contains(slash + ".gradle");

            // Use two slashes to accept `build.gradle.kts`, but filter out the `build` dir.
            boolean isBuildDir = str.contains(slash + DirectoryName.build.value() + slash);
            return !isGradleCache && !isBuildDir;
        }

    }
    private void writeProtoFiles(Multimap<SourceSetName, String> fileNames) throws IOException {
        for (Map.Entry<SourceSetName, String> protoFile : fileNames.entries()) {
            writeProto(protoFile.getKey(), protoFile.getValue());
        }
    }

    private void writeJavaFiles(Multimap<SourceSetName, String> fileNames) throws IOException {
        for (Map.Entry<SourceSetName, String> javaFile : fileNames.entries()) {
            writeJava(javaFile.getKey(), javaFile.getValue());
        }
    }

    @CanIgnoreReturnValue
    public BuildResult executeTask(TaskName taskName) {
        return prepareRun(taskName).build();
    }

    @CanIgnoreReturnValue
    public BuildResult executeAndFail(TaskName taskName) {
        return prepareRun(taskName).buildAndFail();
    }

    private GradleRunner prepareRun(TaskName taskName) {
        String[] args = arguments.forTask(taskName);
        return runner.withArguments(args);
    }

    private void writeProto(SourceSetName ssn, String fileName) throws IOException {
        writeFile(fileName, protoDir(ssn));
    }

    private static String protoDir(SourceSetName ssn) {
        return format("src/%s/proto/", ssn);
    }

    private void writeJava(SourceSetName ssn, String fileName) throws IOException {
        writeFile(fileName, javaDir(ssn));
    }

    private static String javaDir(SourceSetName ssn) {
        return format("src/%s/java/", ssn);
    }

    private void writeFile(String fileName, String dir) throws IOException {
        String filePath = dir + fileName;
        String resourcePath = name + '/' + filePath;
        try (InputStream fileContent = openResource(resourcePath)) {
            Path fileSystemPath = projectDir().resolve(filePath);
            createDirectories(fileSystemPath.getParent());
            copy(fileContent, fileSystemPath);
        }
    }

    private InputStream openResource(String fullPath) {
        InputStream stream = getClass().getClassLoader()
                                       .getResourceAsStream(fullPath);
        checkState(stream != null, "Unable to locate resource: `%s`.", fullPath);
        return stream;
    }

    /**
     * A builder for new {@code GradleProject}.
     */
    public static class Builder {

        private final Multimap<SourceSetName, String> protoFileNames =
                MultimapBuilder.hashKeys()
                               .arrayListValues()
                               .build();
        private final Multimap<SourceSetName, String> javaFileNames =
                MultimapBuilder.hashKeys()
                               .arrayListValues()
                               .build();

        private @Nullable ImmutableMap<String, String> environment;
        private @MonotonicNonNull String origin;
        private @MonotonicNonNull File dir;

        private RunnerArguments arguments = new RunnerArguments();

        /** The flag to be passed to {@link GradleRunner#withDebug(boolean)}. */
        private boolean debug;

        /**
         * Determines whether the plugin under test classpath is defined and should be added to
         * the Gradle execution classpath.
         *
         * <p>The {@code plugin-under-test-metadata.properties} resource must be present in
         * the current classpath. The file defines the {@code implementation-classpath} property,
         * which contains the classpath to be added to the Gradle run.
         *
         * <p>Whenever the added classpath contains a Gradle plugin, the executed Gradle scripts may
         * apply it via the {@code plugins} block.
         *
         * @see GradleRunner#withPluginClasspath
         */
        private boolean addPluginUnderTestClasspath;

        /** Prevents direct instantiation of this class. */
        private Builder() {
        }

        /**
         * Sets the name of the subdirectory under {@code resources} which contains files
         * for the project to be created.
         */
        public Builder setResourceOrigin(String name) {
            this.origin = checkNotNull(name);
            return this;
        }

        /**
         * Sets the directory on the file system under which the project will be created.
         */
        public Builder setProjectDir(File dir) {
            this.dir = checkNotNull(dir);
            return this;
        }

        /**
         * Adds a {@code .proto} file to the {@link SourceSetName#main main} source set of
         * the project to be created.
         *
         * @param fileName
         *         a name of the proto file relative to {@code src/main/proto} subdirectory
         *         under the one specified in {@link #setResourceOrigin(String)}
         */
        public Builder addProtoFile(String fileName) {
            checkNotNull(fileName);
            checkNotEmptyOrBlank(fileName);
            return addProtoFile(main, fileName);
        }

        /**
         * Adds a {@code .proto} file to the specified source set of the project to be created.
         *
         * @param ssn
         *          the name of the source set
         * @param fileName
         *         a name of the proto file relative to {@code src/SourceSetName/proto}
         *         subdirectory under the one specified in {@link #setResourceOrigin(String)}
         */
        public Builder addProtoFile(SourceSetName ssn, String fileName) {
            checkNotNull(ssn);
            checkNotNull(fileName);
            checkNotEmptyOrBlank(fileName);
            protoFileNames.put(ssn, fileName);
            return this;
        }

        /**
         * Adds a collection of {@code .proto} files to the {@link SourceSetName#main main}
         * source set of the project to be created.
         *
         * @see #addProtoFile(String)
         */
        public Builder addProtoFiles(Collection<String> fileNames) {
            checkNotNull(fileNames);
            return addProtoFiles(main, fileNames);
        }

        /**
         * Adds a collection of {@code .proto} files to the specified source set of
         * the project to be created.
         *
         * @see #addProtoFile(String)
         */
        public Builder addProtoFiles(SourceSetName ssn, Collection<String> fileNames) {
            checkNotNull(ssn);
            checkNotNull(fileNames);
            fileNames.forEach(fileName -> addProtoFile(ssn, fileName));
            return this;
        }

        /**
         * Adds multiple {@code .proto} files to the project to be created.
         *
         * @see #addProtoFile(String)
         */
        public Builder addProtoFiles(String... fileNames) {
            checkNotNull(fileNames);
            return addProtoFiles(ImmutableList.copyOf(fileNames));
        }

        /**
         * Creates a {@code .proto} source file with the given name and content
         * in the the {@link SourceSetName#main main} source set of the project.
         *
         * @param fileName
         *         the name of the file relative to {@code src/main/proto} directory
         * @param lines
         *         the content of the file
         */
        public Builder createProto(String fileName, Iterable<String> lines) {
            checkNotNull(fileName);
            checkNotNull(lines);
            return createProto(main, lines, fileName);
        }

        /**
         * Creates a {@code .proto} source file with the given name and content
         * in the the specified source set of the project.
         *
         * @param fileName
         *         the name of the file relative to {@code src/SourceSetName/proto} directory
         * @param lines
         *         the content of the file
         */
        public Builder createProto(SourceSetName ssn, Iterable<String> lines, String fileName) {
            String path = protoDir(ssn) + fileName;
            return createFile(path, lines);
        }

        /**
         * Adds {@code .java} files to the {@link SourceSetName#main main}
         * source set of the project to be created.
         *
         * @param fileNames
         *         names of the Java files relative to {@code src/main/java} subdirectory
         *         under the one specified in {@link #setResourceOrigin(String)}
         */
        public Builder addJavaFiles(String... fileNames) {
            checkNotNull(fileNames);
            return addJavaFiles(main, asList(fileNames));
        }

        /**
         * Adds {@code .java} files to the specified source set of the project to be created.
         *
         * @param ssn
         *         the name of the source set
         * @param fileNames
         *         names of the Java files relative to {@code src/main/java} subdirectory
         *         under the one specified in {@link #setResourceOrigin(String)}
         */
        public Builder addJavaFiles(SourceSetName ssn, Iterable<String> fileNames) {
            checkNotNull(ssn);
            checkNotNull(fileNames);
            javaFileNames.putAll(ssn, fileNames);
            return this;
        }

        /**
         * Creates a file in the project directory under the given path and with the given content.
         *
         * @param path
         *         the path to the file relative to the project root directory
         * @param lines
         *         the content of the file
         */
        public Builder createFile(String path, Iterable<String> lines) {
            checkNotNull(path);
            checkNotNull(lines);
            Path sourcePath = resolve(path);
            try {
                createDirectories(sourcePath.getParent());
                write(sourcePath, lines, Charsets.UTF_8);
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
            return this;
        }

        @NonNull
        private Path resolve(String path) {
            checkNotNull(
                    dir,
                    "A project directory is not specified. Please call `setProjectDir(File)`."
            );
            Path sourcePath = dir.toPath()
                                 .resolve(path);
            return sourcePath;
        }

        /**
         * Enables the debug mode of the GradleRunner.
         *
         * <p>Affects the code executed during a {@linkplain #executeTask Gradle task}.
         * When turned on, all code is executed in a single JVM.
         * This leads to a high consumption of a memory.
         *
         * <p>Use this mode only for temporary debug purposes.
         * E.g. it should never get to e.g. CI server.
         */
        public Builder enableDebug() {
            this.arguments = arguments.withDebug();
            return this;
        }

        /**
         * Configures this runner to include the plugin under development into the classpath.
         *
         * @see GradleRunner#withPluginClasspath()
         */
        public Builder withPluginClasspath() {
            this.addPluginUnderTestClasspath = true;
            return this;
        }

        /**
         * Adds a property to be passed to the Gradle build using
         * the {@code "-P${name}=${value}"} command line option.
         *
         * @param name
         *         name of the property
         * @param value
         *         value of the property
         */
        public Builder withProperty(String name, String value) {
            checkNotNull(name);
            checkNotNull(value);
            checkNotEmptyOrBlank(name);
            this.arguments = arguments.withProperty(name, value);
            return this;
        }

        /**
         * Configures the environment variables available to the build.
         *
         * <p>If not set, the variables are inherited.
         */
        public Builder withEnvironment(ImmutableMap<String, String> environment) {
            checkNotNull(environment);
            this.environment = environment;
            return this;
        }

        public GradleProject build() {
            try {
                checkNotNull(origin, "Project name");
                checkNotNull(dir, "Project folder");
                GradleProject result = new GradleProject(this);
                return result;
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        }
    }
}
