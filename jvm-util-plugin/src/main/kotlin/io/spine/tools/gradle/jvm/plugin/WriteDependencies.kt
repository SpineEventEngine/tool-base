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

import io.spine.tools.dependency.ArtifactDependencies
import io.spine.tools.dependency.Dependencies
import io.spine.tools.dependency.MavenArtifact
import io.spine.tools.dependency.Module
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import io.spine.tools.dependency.Dependency as TDependency

/**
 * A task that writes all dependencies of a Gradle project to a file.
 *
 * The file is named using the [Module.fileSafeId] property with a `.deps` extension
 * and is placed in the `META-INF/io.spine/` directory.
 */
public abstract class WriteDependencies : DefaultTask() {

    /**
     * The extension of the dependency file.
     */
    public companion object {

        /**
         * The extension of the dependency file.
         */
        public const val FILE_EXTENSION: String = ".deps"

        /**
         * The name of the task added by the plugin.
         */
        public const val TASK_NAME: String = "writeDependencies"
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
        val artifactDependencies = ArtifactDependencies(artifact, dependencies)

        val outputDir = outputDirectory.get().asFile
        outputDir.mkdirs()

        val fileName = "${module.fileSafeId}$FILE_EXTENSION"
        val file = outputDir.resolve(fileName)

        artifactDependencies.store(file)
    }

    /**
     * Collects all dependencies of the project.
     */
    private fun collectDependencies(): Dependencies {
        val list = mutableListOf<TDependency>()

        project.configurations
            .filter { it.isCanBeResolved }
            .forEach { configuration ->
                configuration.dependencies.forEach { dependency ->
                    val mavenArtifact = dependency.toMavenArtifact()
                    if (mavenArtifact != null) {
                        list.add(mavenArtifact)
                    }
                }
            }

        return Dependencies(list)
    }
}

/**
 * Creates a [MavenArtifact] from a Gradle [Dependency].
 */
private fun Dependency.toMavenArtifact(): MavenArtifact? {
    val group = this.group ?: return null
    val name = this.name ?: return null
    val version = this.version ?: return null

    return MavenArtifact(group, name, version)
}
