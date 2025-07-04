/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import io.spine.tools.gradle.task.TaskName
import java.io.File
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

/**
 * The umbrella object for the popular constants.
 */
@Suppress("ConstPropertyName")
public object Gradle {

    /**
     * The name of the project build file in the format of the Kotlin script.
     */
    public const val buildFile: String = "build.gradle.kts"

    /**
     * The name of the project settings file in the format of the Kotlin script.
     */
    public const val settingsFile: String = "settings.gradle.kts"

    /**
     * The part of the console output that tells about the successful result of the build.
     */
    public const val BUILD_SUCCESSFUL: String = "BUILD SUCCESSFUL"
}

/**
 * Obtains an instance of [File] having the given directory as the parent
 * and this string as the name of the file.
 */
public fun String.under(dir: File): File = File(dir, this)

/**
 * Runs a Gradle build for the project created in the [given directory][projectDir]
 * using the given [tasks].
 * @param projectDir The directory containing the Gradle project to build.
 * @param arguments The list of command line arguments to pass to Gradle.
 * @param tasks The tasks to execute.
 * @return the result of the build.
 * @see GradleProject
 */
public fun runGradleBuild(projectDir: File, vararg tasks: TaskName): BuildResult {
    val arguments = tasks.map { it.name() }
    return runGradleBuild(projectDir, arguments)
}

/**
 * Runs a Gradle build for the project in the specified directory with given arguments.
 *
 * @param projectDir The directory containing the Gradle project to build.
 * @param arguments The list of command line arguments to pass to Gradle.
 * @param debug Whether to run Gradle in debug mode.
 * @return the result of the build.
 * @see GradleProject
 */
public fun runGradleBuild(
    projectDir: File,
    arguments: List<String>,
    debug: Boolean = false,
): BuildResult {
    val runner = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments(arguments)
    if (debug) {
        runner.withDebug(true)
    }    
    return runner.build()
}
