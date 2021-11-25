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
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.gradle.task.TaskName;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.io.Copy.copyDir;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
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

    private static final String mainProtoDir = "src/main/proto/";
    private static final String mainJavaDir = "src/main/java/";
    private static final String buildSrcDir = "buildSrc";

    private final String name;
    private final GradleRunner runner;
    private final boolean debug;
    private final ImmutableMap<String, String> properties;

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
        this.name = builder.name;
        this.debug = builder.debug;
        this.runner = GradleRunner.create()
                .withProjectDir(builder.folder)
                .withDebug(builder.debug);
        if (builder.addPluginUnderTestClasspath) {
            runner.withPluginClasspath();
        }
        if (builder.environment != null) {
            runner.withEnvironment(builder.environment);
        }
        this.properties = ImmutableMap.copyOf(builder.properties);
        writeGradleScripts();
        writeBuildSrc();
        writeProtoFiles(builder.protoFileNames);
        writeJavaFiles(builder.javaFileNames);
    }

    private void writeGradleScripts() throws IOException {
        BuildGradle buildGradle = new BuildGradle(projectRoot());
        buildGradle.createFile();

        Path projectRoot = ProjectRoot.instance().toPath();
        TestEnvGradle testEnvGradle = new TestEnvGradle(projectRoot, projectRoot());
        testEnvGradle.createFile();
    }

    private void writeBuildSrc() throws IOException {
        Path projectRoot = ProjectRoot.instance().toPath();
        Path buildSrc = projectRoot.resolve(buildSrcDir);
        Path target = projectRoot();
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

            /**
             * The following block is commented out because not copying the `build`
             * makes dependencies defined as Kotlin objects (see
             * `buildSrc/src/main/kotlin/io/spine/internal/dependency`) unresolvable in
             * Groovy-based Gradle scripts in tests.
             *
             * Uncomment the below block and the associated boolean operation in the `return`
             * statement when resolving
             * the [associated issue][https://github.com/SpineEventEngine/base/issues/655].
             */
//            // Use two slashes to accept `build.gradle.kts`, but filter out the `build` dir.
//            @SuppressWarnings("DuplicateStringLiteralInspection")
//            boolean isBuildDir = str.contains(slash + "build" + slash);
            return !isGradleCache /*&& !isBuildDir*/;
        }
    }

    private void writeProtoFiles(Iterable<String> fileNames) throws IOException {
        for (String protoFile : fileNames) {
            writeProto(protoFile);
        }
    }

    private void writeJavaFiles(Iterable<String> fileNames) throws IOException {
        for (String javaFile : fileNames) {
            writeJava(javaFile);
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
        RunnerArguments arguments = RunnerArguments.forTask(taskName);
        if (debug) {
            arguments = arguments.withDebug();
        }
        String[] args = arguments.apply(properties);
        return runner.withArguments(args);
    }

    private void writeProto(String fileName) throws IOException {
        writeFile(fileName, mainProtoDir);
    }

    private void writeJava(String fileName) throws IOException {
        writeFile(fileName, mainJavaDir);
    }

    private void writeFile(String fileName, String dir) throws IOException {
        String filePath = dir + fileName;
        String resourcePath = name + '/' + filePath;
        try (InputStream fileContent = openResource(resourcePath)) {
            Path fileSystemPath = projectRoot().resolve(filePath);
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

    private Path projectRoot() {
        return runner.getProjectDir()
                     .toPath();
    }

    /**
     * A builder for new {@code GradleProject}.
     */
    public static class Builder {

        private final List<String> protoFileNames = new ArrayList<>();
        private final List<String> javaFileNames = new ArrayList<>();
        private final Map<String, String> properties = new HashMap<>();

        private @Nullable ImmutableMap<String, String> environment;
        private String name;
        private File folder;

        /**
         * Determines whether the code can be debugged.
         *
         * <p>Affects the code executed during a {@linkplain #executeTask Gradle task}.
         *
         * <p>NOTE: when the value is {@code true}, all code is executed in a single JVM.
         * This leads to a high consumption of a memory.
         */
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
        public Builder setProjectName(String name) {
            this.name = checkNotNull(name);
            return this;
        }

        /**
         * Sets the name of the directory on the file system under which
         * the project will be created.
         */
        public Builder setProjectFolder(File folder) {
            this.folder = checkNotNull(folder);
            return this;
        }

        /**
         * Adds a {@code .proto} file to the project to be created.
         *
         * @param protoFileName
         *         a name of the proto file relative to {@code src/main/proto} sub-directory
         *         under the one specified in {@link #setProjectName(String)}
         */
        public Builder addProtoFile(String protoFileName) {
            checkNotNull(protoFileName);
            checkArgument(!protoFileName.isEmpty());
            protoFileNames.add(protoFileName);
            return this;
        }

        /**
         * Adds a collection of {@code .proto} files to the project to be created.
         *
         * @see #addProtoFile(String)
         */
        public Builder addProtoFiles(Collection<String> protoFileNames) {
            checkNotNull(protoFileNames);
            protoFileNames.forEach(this::addProtoFile);
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
         * Adds {@code .java} files to the project to be created.
         *
         * @param fileNames
         *         names of the Java files relative to {@code src/main/java} subdirectory
         *         under the one specified in {@link #setProjectName(String)}
         */
        public Builder addJavaFiles(String... fileNames) {
            checkNotNull(fileNames);
            javaFileNames.addAll(asList(fileNames));
            return this;
        }

        /**
         * Enables the debug mode of the GradleRunner.
         *
         * <p>Use debug mode only for temporary debug purposes.
         */
        @SuppressWarnings("RedundantSuppression")
        // Used only for debug purposes. Should never get to e.g. CI server.
        public Builder enableDebug() {
            this.debug = true;
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
            this.properties.put(name, value);
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

        /**
         * Creates a {@code .proto} source file with the given name and content.
         *
         * @param fileName
         *         the name of the file relative to {@code src/main/proto} directory
         * @param lines
         *         the content of the file
         */
        public Builder createProto(String fileName, Iterable<String> lines) {
            checkNotNull(fileName);
            checkNotNull(lines);

            String path = mainProtoDir + fileName;
            return createFile(path, lines);
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

            Path sourcePath = folder.toPath()
                                    .resolve(path);
            try {
                createDirectories(sourcePath.getParent());
                write(sourcePath, lines, Charsets.UTF_8);
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
            return this;
        }

        public GradleProject build() {
            try {
                checkNotNull(name, "Project name");
                checkNotNull(folder, "Project folder");
                GradleProject result = new GradleProject(this);
                return result;
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        }
    }
}
