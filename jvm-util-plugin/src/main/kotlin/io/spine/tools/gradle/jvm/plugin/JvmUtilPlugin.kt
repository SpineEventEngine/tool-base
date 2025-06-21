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

package io.spine.tools.gradle.jvm.plugin

import io.spine.tools.gradle.jvm.plugin.WriteDependencies.Companion.TASK_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension

/**
 * A Gradle plugin that provides JVM utilities.
 *
 * The plugin adds a task to write all dependencies of a Gradle project to a file.
 */
public class JvmUtilPlugin : Plugin<Project> {

    /**
     * Applies the plugin to the given project.
     */
    override fun apply(project: Project): Unit = with(project) {
        val outputDir = layout.buildDirectory.dir(io.spine.tools.dependency.ArtifactDependencies.RESOURCE_DIRECTORY)

        val task = tasks.register(TASK_NAME, WriteDependencies::class.java) { task ->
            task.outputDirectory.convention(outputDir)
        }

        tasks.named("processResources").configure { it.dependsOn(task) }

        // Add the output directory to the resources
        extensions.getByType(JavaPluginExtension::class.java)
            .sourceSets.getByName("main")
            .resources
            .srcDir(outputDir)
    }
}
