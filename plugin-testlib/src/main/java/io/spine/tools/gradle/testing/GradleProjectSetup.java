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
import io.spine.tools.gradle.SourceSetName;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.SourceSetName.main;
import static io.spine.tools.gradle.testing.Sources.protoDir;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Customizes a new {@code GradleProject}.
 *
 * @apiNote We avoid builder pattern naming around this class to avoid the confusion associated with
 *         having a {@code build()} method in association with {@link GradleProject}.
 *         This is caused by the fact that {@link GradleRunner} — which is used and
 *         {@linkplain GradleProject#runner() exposed} by {@code GradleProject} — does have
 *         the {@link GradleRunner#build() build()} method which executes a Gradle build.
 */
@SuppressWarnings({
        "unused"  /* Some methods are used only in downstream repositories, e.g. `mc-java`.
         This suppression should be removed after the split of modules from the `base`
         is finished, and we have Model Compiler and related artifacts originated from `base`
          settled in their new repositories. */,
        "ClassWithTooManyMethods"}
)
public final class GradleProjectSetup {

    private final Multimap<SourceSetName, String> protoFileNames =
            MultimapBuilder.hashKeys()
                           .arrayListValues()
                           .build();
    private final Multimap<SourceSetName, String> javaFileNames =
            MultimapBuilder.hashKeys()
                           .arrayListValues()
                           .build();

    private @MonotonicNonNull String origin;
    private @MonotonicNonNull File dir;
    private @Nullable ImmutableMap<String, String> environment;

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
    GradleProjectSetup() {
    }

    /**
     * Sets the name of the subdirectory under {@code resources} which contains files
     * for the project to be created.
     */
    public GradleProjectSetup setOrigin(String name) {
        this.origin = checkNotNull(name);
        return this;
    }

    /**
     * Sets the directory on the file system under which the project will be created.
     */
    public GradleProjectSetup setProjectDir(File dir) {
        this.dir = checkNotNull(dir);
        return this;
    }

    /**
     * Adds a {@code .proto} file to the {@link SourceSetName#main main} source set of
     * the project to be created.
     *
     * @param fileName
     *         a name of the proto file relative to {@code src/main/proto} subdirectory
     *         under the one specified in {@link #setOrigin(String)}
     */
    public GradleProjectSetup addProtoFile(String fileName) {
        checkNotNull(fileName);
        checkNotEmptyOrBlank(fileName);
        return addProtoFile(main, fileName);
    }

    /**
     * Adds a {@code .proto} file to the specified source set of the project to be created.
     *
     * @param ssn
     *         the name of the source set
     * @param fileName
     *         a name of the proto file relative to {@code src/SourceSetName/proto}
     *         subdirectory under the one specified in {@link #setOrigin(String)}
     */
    public GradleProjectSetup addProtoFile(SourceSetName ssn, String fileName) {
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
    public GradleProjectSetup addProtoFiles(Collection<String> fileNames) {
        checkNotNull(fileNames);
        return addProtoFiles(main, fileNames);
    }

    /**
     * Adds a collection of {@code .proto} files to the specified source set of
     * the project to be created.
     *
     * @see #addProtoFile(String)
     */
    public GradleProjectSetup addProtoFiles(SourceSetName ssn, Collection<String> fileNames) {
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
    public GradleProjectSetup addProtoFiles(String... fileNames) {
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
    public GradleProjectSetup createProto(String fileName, Iterable<String> lines) {
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
    public GradleProjectSetup createProto(SourceSetName ssn, Iterable<String> lines,
                                          String fileName) {
        String path = protoDir(ssn) + fileName;
        return createFile(path, lines);
    }

    /**
     * Adds {@code .java} files to the {@link SourceSetName#main main}
     * source set of the project to be created.
     *
     * @param fileNames
     *         names of the Java files relative to {@code src/main/java} subdirectory
     *         under the one specified in {@link #setOrigin(String)}
     */
    public GradleProjectSetup addJavaFiles(String... fileNames) {
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
     *         under the one specified in {@link #setOrigin(String)}
     */
    public GradleProjectSetup addJavaFiles(SourceSetName ssn, Iterable<String> fileNames) {
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
    public GradleProjectSetup createFile(String path, Iterable<String> lines) {
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
     * <p>Affects the code executed during a {@linkplain GradleProject#executeTask Gradle task}.
     * When turned on, all code is executed in a single JVM.
     * This leads to a high consumption of a memory.
     *
     * <p>Use this mode only for temporary debug purposes.
     * E.g. it should never get to e.g. CI server.
     */
    public GradleProjectSetup enableDebug() {
        this.arguments = arguments.withDebug();
        return this;
    }

    /**
     * Configures this runner to include the plugin under development into the classpath.
     *
     * @see GradleRunner#withPluginClasspath()
     */
    public GradleProjectSetup withPluginClasspath() {
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
    public GradleProjectSetup withProperty(String name, String value) {
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
    public GradleProjectSetup withEnvironment(ImmutableMap<String, String> environment) {
        checkNotNull(environment);
        this.environment = environment;
        return this;
    }

    /**
     * Creates a new project on the file system.
     */
    public GradleProject create() {
        try {
            checkNotNull(origin, "Project name");
            checkNotNull(dir, "Project folder");
            GradleProject result = new GradleProject(this);
            return result;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    String origin() {
        return requireNonNull(origin);
    }

    @Nullable ImmutableMap<String, String> environment() {
        return environment;
    }

    RunnerArguments arguments() {
        return arguments;
    }

    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // OK in this builder arrangement.
    Multimap<SourceSetName, String> protoFileNames() {
        return protoFileNames;
    }

    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // OK in this builder arrangement.
    Multimap<SourceSetName, String> javaFileNames() {
        return javaFileNames;
    }

    File dir() {
        return dir;
    }

    boolean debug() {
        return debug;
    }

    boolean addPluginUnderTestClasspath() {
        return addPluginUnderTestClasspath;
    }
}
