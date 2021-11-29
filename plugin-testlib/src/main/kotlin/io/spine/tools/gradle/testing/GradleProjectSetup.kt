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
package io.spine.tools.gradle.testing

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.ImmutableMap
import io.spine.util.Exceptions
import io.spine.util.Preconditions2
import java.io.File
import java.io.IOException
import java.nio.file.Path
import org.checkerframework.checker.nullness.qual.MonotonicNonNull

/**
 * Customizes a new `GradleProject`.
 *
 * @apiNote We avoid builder pattern naming around this class to avoid the confusion associated with
 * having a `build()` method in association with [GradleProject].
 * This is caused by the fact that [GradleRunner][org.gradle.testkit.runner.GradleRunner] —
 * which is used and [exposed][GradleProject.runner] by `GradleProject` — does have
 * the [build()][org.gradle.testkit.runner.GradleRunner.build] method which executes a Gradle build.
 */
public class GradleProjectSetup internal constructor(
    /** Path on the file system under which the project will be created.  */
    internal val projectDir: File
) {

    /** Maps a relative name of a file to its content.  */
    private val filesToCreate: MutableMap<String, List<String>> = HashMap()

    /**
     * The name of the directory under `resources` for loading files of the project.
     *
     * Is `null` if files will be created on the fly.
     */
    internal var resourceDir: @MonotonicNonNull String? = null
        private set

    /**
     * The predicate to accept resources (both files and directories) placed in the [resourceDir].
     */
    private var matching: (Path) -> Boolean = { true }
    private var environment: Map<String, String>? = null
    private var arguments = RunnerArguments()

    /**
     * If set, the `buildSrc` directory will be copied from the root project
     * into the directory of the project to be created.
     */
    private var needsBuildSrc = false

    /** The flag to be passed to [org.gradle.testkit.runner.GradleRunner.withDebug].  */
    private var debug = false

    /**
     * Determines whether the plugin under test classpath is defined and should be added to
     * the Gradle execution classpath.
     *
     *
     * The `plugin-under-test-metadata.properties` resource must be present in
     * the current classpath. The file defines the `implementation-classpath` property,
     * which contains the classpath to be added to the Gradle run.
     *
     *
     * Whenever the added classpath contains a Gradle plugin, the executed Gradle scripts may
     * apply it via the `plugins` block.
     *
     * @see org.gradle.testkit.runner.GradleRunner.withPluginClasspath
     */
    private var addPluginUnderTestClasspath = false

    /**
     * Sets the name of the resource directory and the predicate which accepts the files
     * from the specified directory for copying to the project to be created.
     *
     * Only files and directories that belong to the `resourceDir` would be passed to
     * the `matching` predicate when creating the project in response to
     * the [.create] method is call.
     */

    /**
     * Sets the name of the resource directory from which to load files of
     * the project to be created.
     */
    @JvmOverloads
    public fun fromResources(
        resourceDir: String,
        matching: (Path) -> Boolean = { true }
    ): GradleProjectSetup {
        this.resourceDir = resourceDir
        this.matching = matching
        return this
    }

    /**
     * Creates a source code file with the given content.
     *
     * @param path
     * the path to the file relative to the project root directory
     * @param lines
     * the content of the file
     */
    public fun addFile(path: String, lines: Iterable<String>): GradleProjectSetup {
        checkNotNull(path)
        checkNotNull(lines)
        filesToCreate[path] = lines.toList()
        return this
    }

    /**
     * Enables the debug mode of the GradleRunner.
     *
     *
     * Affects the code executed during a [Gradle task][GradleProject.executeTask].
     * When turned on, all code is executed in a single JVM.
     * This leads to a high consumption of a memory.
     *
     *
     * Use this mode only for temporary debug purposes.
     * E.g. it should never get to e.g. CI server.
     */
    public fun enableDebug(): GradleProjectSetup {
        //TODO:2021-11-29:alexander.yevsyukov: Do we need both?
        debug = true
        arguments = arguments.withDebug()
        return this
    }

    /**
     * Instructs to copy the `buildSrc` directory from the parent project
     * into the directory of the project to be created.
     */
    public fun copyBuildSrc(): GradleProjectSetup {
        needsBuildSrc = true
        return this
    }

    /**
     * Configures this runner to include the plugin under development into the classpath.
     *
     * @see org.gradle.testkit.runner.GradleRunner.withPluginClasspath
     */
    public fun withPluginClasspath(): GradleProjectSetup {
        addPluginUnderTestClasspath = true
        return this
    }

    /**
     * Adds a property to be passed to the Gradle build using
     * the `"-P${name}=${value}"` command line option.
     *
     * @param name
     * name of the property
     * @param value
     * value of the property
     */
    public fun withProperty(name: String, value: String): GradleProjectSetup {
        checkNotNull(name)
        checkNotNull(value)
        Preconditions2.checkNotEmptyOrBlank(name)
        arguments = arguments.withProperty(name, value)
        return this
    }

    /**
     * Configures the environment variables available to the build.
     *
     *
     * If not set, the variables are inherited.
     */
    public fun withEnvironment(environment: ImmutableMap<String, String>): GradleProjectSetup {
        checkNotNull(environment)
        this.environment = environment
        return this
    }

    /**
     * Creates a new project on the file system.
     */
    public fun create(): GradleProject {
        return try {
            GradleProject(this)
        } catch (e: IOException) {
            throw Exceptions.illegalStateWithCauseOf(e)
        }
    }

//    /**
//     * Obtains the previously configured resource origin.
//     *
//     * @return the origin or `null` if [fromResources] was never called
//     */
//    internal fun resourceDir(): String? {
//        return resourceDir
//    }

    internal fun environment(): Map<String, String>? {
        return environment
    }

    internal fun arguments(): RunnerArguments {
        return arguments
    }

    internal fun matching(): (Path) -> Boolean {
        return matching
    }

    internal fun debug(): Boolean {
        return debug
    }

    internal fun needsBuildSrc(): Boolean {
        return needsBuildSrc
    }

    internal fun addPluginUnderTestClasspath(): Boolean {
        return addPluginUnderTestClasspath
    }

    @VisibleForTesting
    internal fun testEnvPath(): Path {
        return TestEnvGradle(projectDir.toPath()).path()
    }

    /**
     * Provides mapping from file paths to their content.
     *
     *
     * Keys of the returned map are file paths relative the directory
     * of the project to be created.
     *
     *
     * Values of the returned map are lines of the files to be created.
     */
    internal fun filesToCreate(): MutableMap<String, List<String>> {
        return this.filesToCreate
    }
}
