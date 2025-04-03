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
import org.gradle.api.Plugin
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

/**
 * Obtains the result of the [task] from this build result.
 *
 * @throws IllegalStateException if the build result does not have a task with the given name.
 */
public operator fun BuildResult.get(task: TaskName): TaskOutcome {
    val buildTask = task(task.path())
    check(buildTask != null) {
        "The build result does not have the task named `${task.name()}`."
    }
    return buildTask.outcome
}

/**
 * Obtains a JAR file or directory in which this Java class is placed.
 */
public fun Class<*>.classpathElement(): File =
    File(protectionDomain.codeSource.location.file)

/**
 * Obtains the directory or a JAR file containing the Gradle plugin definition file
 * for the plugin with the given ID.
 */
public fun Class<Plugin<*>>.resourceDir(pluginId: String): File {
    val pluginDescriptionUrl = classLoader.getResource(
        "META-INF/gradle-plugins/${pluginId}.properties"
    )!!
    // This points to the directory containing `META-INF`.
    // This is what we need to pass to `GradleRunner.withPluginClasspath(...)`.
    val pluginDir = File(pluginDescriptionUrl.file).parentFile.parentFile.parentFile
    return pluginDir
}
