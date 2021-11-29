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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * Customizes a new {@code GradleProject}.
 *
 * @apiNote We avoid builder pattern naming around this class to avoid the confusion associated with
 *         having a {@code build()} method in association with {@link GradleProject}.
 *         This is caused by the fact that {@link GradleRunner} — which is used and
 *         {@linkplain GradleProject#runner() exposed} by {@code GradleProject} — does have
 *         the {@link GradleRunner#build() build()} method which executes a Gradle build.
 */
public final class GradleProjectSetup {

    /** Path on the file system under which the project will be created. */
    private final File projectDir;

    /** Maps a relative name of a file to its content. */
    private final Map<String, ImmutableList<String>> filesToCreate = new HashMap<>();

    /**
     * The name of the directory under {@code resources} for loading files of the project.
     *
     * <p>Is {@code null} if files will be created on the fly.
     */
    private @MonotonicNonNull String resourceDir;

    /**
     * The predicate to accept resources from the {@link #resourceDir()}.
     */
    private Predicate<Path> matching = path -> true;

    private @Nullable ImmutableMap<String, String> environment;

    private RunnerArguments arguments = new RunnerArguments();

    /**
     * If set, the {@code buildSrc} directory will be copied from the root project
     * into the directory of the project to be created.
     */
    private boolean needsBuildSrc;

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

    /**
     * Creates a new instance with the specified project directory.
     */
    GradleProjectSetup(File projectDir) {
        this.projectDir = checkNotNull(projectDir);
    }

    /**
     * Sets the name of the resource directory from which to load files of
     * the project to be created.
     */
    public GradleProjectSetup fromResources(String resourceDir) {
        return fromResources(resourceDir, path -> true);
    }

    /**
     * Sets the name of the resource directory and the predicate which accepts the files
     * from the specified directory for copying to the project to be created.
     *
     * <p>Only files and directories that belong to the {@code resourceDir} would be passed to
     * the {@code matching} predicate when creating the project in response to
     * the {@link #create()} method is call.
     */
    public GradleProjectSetup fromResources(String resourceDir, Predicate<Path> matching) {
        this.resourceDir = checkNotNull(resourceDir);
        this.matching = checkNotNull(matching);
        return this;
    }

    /**
     * Creates a source code file with the given content.
     *
     * @param path
     *         the path to the file relative to the project root directory
     * @param lines
     *         the content of the file
     */
    public GradleProjectSetup addFile(String path, Iterable<String> lines) {
        checkNotNull(path);
        checkNotNull(lines);
        filesToCreate.put(path, ImmutableList.copyOf(lines));
        return this;
    }

    private Path resolve(String path) {
        Path sourcePath = projectDir.toPath()
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
     * Instructs to copy the {@code buildSrc} directory from the parent project
     * into the directory of the project to be created.
     */
    public GradleProjectSetup copyBuildSrc() {
        this.needsBuildSrc = true;
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
            GradleProject result = new GradleProject(this);
            return result;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Obtains the previously configured resource origin.
     *
     * @return the origin or {@code null} if {@link #fromResources(String)} was never called
     */
    @Nullable String resourceDir() {
        return resourceDir;
    }

    @Nullable ImmutableMap<String, String> environment() {
        return environment;
    }

    RunnerArguments arguments() {
        return arguments;
    }

    File projectDir() {
        return projectDir;
    }

    Predicate<Path> matching() {
        return matching;
    }

    boolean debug() {
        return debug;
    }

    boolean needsBuildSrc() {
        return needsBuildSrc;
    }

    boolean addPluginUnderTestClasspath() {
        return addPluginUnderTestClasspath;
    }

    @VisibleForTesting
    public Path testEnvPath() {
        return new TestEnvGradle(projectDir.toPath()).path();
    }

    /**
     * Provides mapping from file paths to their content.
     *
     * <p>Keys of the returned map are file paths relative the directory
     * of the project to be created.
     *
     * <p>Values of the returned map are lines of the files to be created.
     */
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // OK in this builder arrangement.
    Map<String, ImmutableList<String>> filesToCreate() {
        return filesToCreate;
    }
}
