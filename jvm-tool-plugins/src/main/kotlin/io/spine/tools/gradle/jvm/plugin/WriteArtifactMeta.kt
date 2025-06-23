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

import io.spine.tools.meta.ArtifactMeta
import io.spine.tools.meta.Dependencies
import io.spine.tools.meta.MavenArtifact
import io.spine.tools.meta.Module
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * A task that writes artifact [meta-data][ArtifactMeta] of a Gradle project to a file.
 *
 * The file is created as a resource file.
 */
public abstract class WriteArtifactMeta : DefaultTask() {

    /**
     * The task that writes dependencies to a file.
     */
    public companion object {

        /**
         * The name of the task added by the plugin.
         */
        public const val TASK_NAME: String = "writeArtifactMeta"
    }

    /**
     * The directory that hosts the generated file.
     */
    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    /**
     * Writes the dependencies of the project to a file.
     */
    @TaskAction
    public fun writeFile() {
        outputDirectory.finalizeValue()

        val group = project.group.toString()
        val name = project.name
        val module = Module(group, name)
        val artifact = MavenArtifact(group, name, project.version.toString())

        val dependencies = collectDependencies()
        val artifactMeta = ArtifactMeta(artifact, dependencies)

        val outputDir = outputDirectory.get().asFile
        outputDir.mkdirs()

        val fileName = ArtifactMeta.resourcePath(module)
        val file = outputDir.resolve(fileName)

        artifactMeta.store(file)
    }

    /**
     * Collects all the non-test dependencies of the project.
     */
    private fun collectDependencies(): Dependencies {
        val list =  project.configurations
            .filter { !it.name.lowercase().contains("test") }
            .flatMap { c -> c.dependencies }
            .mapNotNull { d -> d.toMavenArtifact() }
            .toSet()
            .toList().sortedWith(
                compareBy<MavenArtifact> { it.group }
                    .thenBy { it.name }
            )
        return Dependencies(list)
    }
}

/**
 * Creates a [MavenArtifact] from a Gradle [Dependency].
 *
 * The `null` checks performed by the function filter out dependencies that
 * do have either `group`, `name`, or `version` attribute available,
 * which is a safety feature for the dynamic Gradle environment.
 *
 * The dependencies of our interest are going to have the required attributes.
 */
private fun Dependency.toMavenArtifact(): MavenArtifact? {
    val group = this.group ?: return null
    val name = this.name ?: return null
    val version = this.version ?: return null
    return MavenArtifact(group, name, version)
}
