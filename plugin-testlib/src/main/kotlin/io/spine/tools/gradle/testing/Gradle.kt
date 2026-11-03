/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package io.spine.tools.gradle.testing

import io.spine.tools.gradle.task.TaskName
import java.io.File
import java.io.Writer
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
 * @param stdOutput The writer to [redirect][GradleRunner.forwardStdOutput] the standard output.
 * @param stdError The writer to [redirect][GradleRunner.forwardStdError] the standard error output.
 * @return the result of the build.
 * @see GradleProject
 */
public fun runGradleBuild(
    projectDir: File,
    arguments: List<String>,
    debug: Boolean = false,
    stdOutput: Writer? = null,
    stdError: Writer? = null
): BuildResult {
    val runner = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments(arguments.withTestKitCoverage())
    runner.enableTestKitCoverage()
    stdOutput?.let {
        runner.forwardStdOutput(it)
    }
    stdError?.let {
        runner.forwardStdError(it)
    }
    if (debug) {
        runner.withDebug(true)
    }
    return runner.build()
}
