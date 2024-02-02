/*
 * Copyright 2022, TeamDev. All rights reserved.
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

import com.google.errorprone.annotations.CanIgnoreReturnValue
import io.spine.tools.gradle.task.TaskName
import java.io.File
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

/**
 * Allows to configure a Gradle project for testing needs.
 *
 * The project operates in the [given directory][setupAt] and allows executing Gradle tasks.
 *
 * The configuration of the project starts with the call to the [setupAt] method, which accepts
 * the directory where the project would be placed. The method returns [GradleProjectSetup]
 * which is used for tuning the project to be created.
 *
 * @see GradleProjectSetup
 */
public class GradleProject internal constructor(setup: GradleProjectSetup) {

    /**
     * The name of the project directory which is used for identifying the project
     * during debug time.
     *
     * If the project was loaded from resources, this value contains the name of
     * the resource directory. Otherwise, it contains the short name of a temporary
     * directory used for running the project.
     *
     * @see toString
     */
    public val directoryName: String = setup.resourceDir ?: setup.projectDir.name

    /**
     * The arguments passed to [runner].
     */
    private val arguments: RunnerArguments = setup.arguments

    /**
     * The runner for executing tasks.
     */
    public val runner: GradleRunner = GradleRunner.create()
        .withProjectDir(setup.projectDir)
        .withDebug(setup.debug)

    init {
        if (setup.addPluginUnderTestClasspath) {
            runner.withPluginClasspath()
        }
        if (setup.environment != null) {
            runner.withEnvironment(setup.environment)
        }
        writeSources(setup)
        replaceTokens(setup)
    }

    public companion object {

        /**
         * The ID of a Java Gradle plugin.
         */
        @Suppress("ConstPropertyName") // https://bit.ly/kotlin-prop-names
        public const val javaPlugin: String = "java"

        /**
         * The ID of Protobuf Gradle Plugin.
         */
        public const val protobufPlugin: String = "com.google.protobuf"

        /**
         * Starts creation of a new the project.
         *
         * @param projectDir
         *          file system directory under which the project will be created
         */
        @JvmStatic
        public fun setupAt(projectDir: File): GradleProjectSetup {
            return GradleProjectSetup(projectDir)
        }

        private fun writeSources(setup: GradleProjectSetup) {
            val sources = Sources(setup)
            sources.write()
        }

        /**
         * Uses the pre-configured [replacements][GradleProjectSetup.replacements] and replaces
         * the tokens in all files of the [projectDir] and its sub-folders.
         *
         * The contents of `projectDir/buildSrc` folder are ignored in this process — as these files
         * hardly ever may contain the tokenized values.
         */
        private fun replaceTokens(setup: GradleProjectSetup) {
            val buildSrcDir = setup.projectDir.resolve("buildSrc")
            setup.projectDir
                .walk()
                .filter { f -> !f.isDirectory }
                .filter { f -> !f.isIn(buildSrcDir) }
                .forEach { f ->
                    setup.replacements.forEach { r ->
                        r.replaceIn(f)
                    }
                }
        }
    }

    /**
     * The directory of this project.
     */
    public val projectDir: File = runner.projectDir

    /**
     * Executes the task with the given name.
     */
    @CanIgnoreReturnValue
    public fun executeTask(task: TaskName): BuildResult {
        return prepareRun(task).build()
    }

    /**
     * Executes the task with the given name and returns the failed build result.
     */
    @CanIgnoreReturnValue
    public fun executeAndFail(task: TaskName): BuildResult {
        return prepareRun(task).buildAndFail()
    }

    private fun prepareRun(taskName: TaskName): GradleRunner {
        val args = arguments.forTask(taskName)
        return runner.withArguments(args)
    }

    /**
     * Returns [directoryName].
     */
    override fun toString(): String = directoryName
}

/**
 * Tells whether the file resides in the [folder].
 *
 * If the [folder] is `null`, returns `false`.
 */
private fun File.isIn(folder: File?) =
    if (folder == null) {
        false
    } else {
        this.absolutePath.startsWith(folder.absolutePath)
    }
