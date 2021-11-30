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
import java.io.File
import java.nio.file.Path
import org.checkerframework.checker.nullness.qual.MonotonicNonNull

/**
 * Customizes creation of a new [GradleProject].
 *
 * @apiNote We avoid builder pattern naming around this class to avoid the confusion associated with
 * having a `build()` method in association with [GradleProject].
 *
 * This is caused by the fact that [GradleRunner][org.gradle.testkit.runner.GradleRunner] —
 * which is used and [exposed][GradleProject.runner] by `GradleProject` — does have
 * the [build()][org.gradle.testkit.runner.GradleRunner.build] method which executes a Gradle build.
 */
public class GradleProjectSetup internal constructor(
    /** Path on the file system under which the project will be created.  */
    internal val projectDir: File
) {

    /**
     * Provides mapping from file paths to their content.
     *
     * Keys of the returned map are file paths relative the directory
     * of the project to be created.
     *
     * Values of the returned map are lines of the files to be created.
     */
    internal val filesToCreate: MutableMap<String, List<String>> = HashMap()

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
    internal var matching: (Path) -> Boolean = { true }
        private set

    internal var environment: Map<String, String>? = null
        private set

    internal var arguments = RunnerArguments()
        private set

    /**
     * If set, the `buildSrc` directory will be copied from the root project
     * into the directory of the project to be created.
     */
    internal var needsBuildSrc = false
        private set

    /** The flag to be passed to [org.gradle.testkit.runner.GradleRunner.withDebug].  */
    internal var debug = false
        private set

    /**
     * Determines whether the plugin under test classpath is defined and should be added to
     * the Gradle execution classpath.
     *
     * The `plugin-under-test-metadata.properties` resource must be present in
     * the current classpath. The file defines the `implementation-classpath` property,
     * which contains the classpath to be added to the Gradle run.
     *
     * Whenever the added classpath contains a Gradle plugin, the executed Gradle scripts may
     * apply it via the `plugins` block.
     *
     * @see org.gradle.testkit.runner.GradleRunner.withPluginClasspath
     */
    internal var addPluginUnderTestClasspath = false
        private set

    /**
     * Sets the name of the resource directory and the predicate which accepts the files
     * from the specified directory for copying to the project to be created.
     *
     * Only files and directories that belong to the [resourceDir] would be passed to
     * the [matching] predicate when creating the project when the [create] method is called.
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
     *          the path to the file relative to the project root directory
     * @param lines
     *          the content of the file
     */
    public fun addFile(path: String, lines: Iterable<String>): GradleProjectSetup {
        filesToCreate[path] = lines.toList()
        return this
    }

    private companion object {
        const val debugModeErrorMsg =
            "Cannot use environment variables in the `debug` mode. Please see the documentation" +
                    " of `org.gradle.testkit.runner.GradleRunner.isDebug()`" +
                    " for more details on this."
    }

    /**
     * Enables the debug mode of the [GradleRunner][GradleProject.runner].
     *
     * Affects the code executed during a [Gradle task][GradleProject.executeTask].
     * When turned on, all code is executed in a single JVM.
     * This leads to a high memory consumption.
     *
     * Use this mode only for temporary debug purposes. E.g. it should never get to e.g. CI server.
     *
     * This method cannot be called if [withEnvironment] was called before.
     * For more information on this please see
     * [GradleRunner.isDebug][org.gradle.testkit.runner.GradleRunner.isDebug].
     */
    public fun enableRunnerDebug(): GradleProjectSetup {
        check(environment == null) { debugModeErrorMsg }
        debug = true
        return this
    }

    /**
     * Adds `--debug` command line option to the Gradle process.
     */
    public fun debugLogging(): GradleProjectSetup  {
        arguments = arguments.withDebugLogging()
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
     *          name of the property
     * @param value
     *          value of the property
     */
    public fun withProperty(name: String, value: String): GradleProjectSetup {
        require(name.isNotBlank())
        require(value.isNotBlank())
        arguments = arguments.withProperty(name, value)
        return this
    }

    /**
     * Configures the environment variables available to the build.
     *
     * If not set, the variables are inherited.
     *
     * This method cannot be called if [enableRunnerDebug] was called before.
     * For more information on this please see [org.gradle.testkit.runner.GradleRunner.isDebug].
     */
    public fun withEnvironment(environment: Map<String, String>): GradleProjectSetup {
        check(!debug) { debugModeErrorMsg }
        this.environment = environment
        return this
    }

    /**
     * Creates a new project on the file system.
     */
    public fun create(): GradleProject = GradleProject(this)

    @VisibleForTesting
    internal fun testEnvPath(): Path = TestEnvGradle(projectDir.toPath()).path()
}
