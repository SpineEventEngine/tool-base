/*
 * Copyright 2026, TeamDev. All rights reserved.
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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * A task that writes artifact [meta-data][ArtifactMeta] of a Gradle project to a file.
 *
 * The file is created as a resource file.
 */
@DisableCachingByDefault(because = "Dependencies or the artifact coordinates may change.")
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
     * The Maven group of the project being described.
     */
    @get:Input
    public abstract val artifactGroup: Property<String>

    /**
     * The artifact ID of the project being described.
     *
     * Defaults to the project name unless overridden via the
     * [`artifactMeta`][ArtifactMetaExtension] extension.
     */
    @get:Input
    public abstract val artifactId: Property<String>

    /**
     * The version of the project being described.
     */
    @get:Input
    public abstract val artifactVersion: Property<String>

    /**
     * Maven coordinates of the dependencies to be written into the metadata.
     *
     * These are collected from the project configurations and the explicitly
     * declared dependencies at configuration time by [ArtifactMetaPlugin], so
     * that the task action does not access the project during execution.
     */
    @get:Input
    public abstract val dependencyCoordinates: SetProperty<String>

    /**
     * Writes the metadata of the project to a file.
     */
    @TaskAction
    public fun writeFile() {
        outputDirectory.finalizeValue()

        val group = artifactGroup.get()
        val id = artifactId.get()
        val artifact = MavenArtifact(group, id, artifactVersion.get())

        val dependencies = collectDependencies()
        val artifactMeta = ArtifactMeta(artifact, dependencies)

        val outputDir = outputDirectory.get().asFile
        outputDir.mkdirs()

        val module = Module(group, id)
        val fileName = ArtifactMeta.resourcePath(module)
        val file = outputDir.resolve(fileName)

        artifactMeta.store(file)
    }

    /**
     * Rebuilds the collected dependencies from their [coordinates][dependencyCoordinates],
     * keeping one artifact per module and sorting the result.
     */
    private fun collectDependencies(): Dependencies {
        val artifacts = dependencyCoordinates.get()
            .map { MavenArtifact.withCoordinates(it) }

        // Deduplicate by module keeping the artifact with the highest sorting order.
        val deduplicated = artifacts
            .groupBy { it.module }
            .values
            .mapNotNull { perModule -> perModule.maxWithOrNull(mavenArtifactComparator) }
            .sortedWith(mavenArtifactComparator)
        return Dependencies(deduplicated)
    }
}

/**
 * Compares [MavenArtifact] by its attributes.
 *
 * Even though [MavenArtifact] is [Comparable] in the latest versions,
 * the transition to the newer code faces the older, non-`Comparable` class
 * in a build classpath.
 *
 * This comparator needs to be removed once CoreJvm Compiler migrates to
 * the build which uses CoreJvm Compiler based on the `Comparable` `MavenArtifact.
 */
private val mavenArtifactComparator: Comparator<MavenArtifact> =
    compareBy<MavenArtifact> { it.group }
        .thenBy { it.name }
        .thenBy { it.version }
        .thenBy { it.classifier }
        .thenBy { it.extension }
