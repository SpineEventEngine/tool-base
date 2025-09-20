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

import io.spine.tools.gradle.jvm.plugin.ArtifactMetaExtension.Companion.NAME
import io.spine.tools.gradle.jvm.plugin.WriteArtifactMeta.Companion.TASK_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

/**
 * A Gradle plugin that writes [artifact metadata][io.spine.tools.meta.ArtifactMeta]
 * to the resources of a project.
 *
 * The plugin adds the [WriteArtifactMeta] task to the project to which it is applied
 * and exposes the `artifactMeta` extension for configuration.
 *
 * ### Artifact metadata file
 *
 * The path of the created file is:
 *  * `${project.buildDir}/resources/main/META-INF/io.spine/<artifact-id>.meta`
 *
 *  Where `<artifact-id>` is obtained as the [fileSafeId][io.spine.tools.meta.Module.fileSafeId]
 *  property of the [Module][io.spine.tools.meta.Module] representing the published project.
 *
 * #### Overriding the artifact ID
 *
 * By default, the artifact ID is taken from `project.name`.
 *
 * You can override it via the [`artifactId`][ArtifactMetaExtension.artifactId] property of the
 * `artifactMeta` extension. Overriding may be necessary when the project is published with the ID
 * other than `project.name`.
 *
 * Changing `artifactId` affects both:
 *  - the Maven artifact written into the metadata; and
 *  - the resource file name (because it is derived from the module built from the
 *    project `group` and the configured `artifactId`).
 *
 * Kotlin DSL (`build.gradle.kts`):
 * ```kotlin
 * artifactMeta {
 *     artifactId.set("custom-artifact-id")
 * }
 * ```
 *
 * ### `excludeConfiguration` DSL
 *
 * Use the `artifactMeta` extension to exclude configurations whose dependencies
 * should not be written into the metadata file.
 * This is useful to filter out test, IDE, or other auxiliary configurations.
 *
 * Kotlin DSL example (`build.gradle.kts`):
 *
 * ```kotlin
 *   artifactMeta {
 *       excludeConfigurations {
 *           // Exclude configurations by their exact names (case-sensitive):
 *           named("testCompileClasspath", "testRuntimeClasspath")
 *
 *           // Exclude any configuration whose name contains any of the given substrings
 *           // (case-insensitive substring match):
 *           containing("test", "intellij")
 *       }
 *   }
 * ```
 * 
 * ### Configurations excluded by default
 * 
 * The plugin automatically excludes all configurations having `"test"` in their names.
 * 
 * To include test configurations into the artifact meta file, use the following DSL.
 * 
 * ```kotlin
 * artifactMeta {
 *    excludeConfigurations {
 *        // Reset all defaults. Include all configurations.
 *        clear()
 *        // OR
 *        containing.set(emptySet())
 *    }
 * }
 * ```
 *
 * ### Adding dependencies explicitly
 *
 * You can explicitly add Maven dependencies to be included into the artifact metadata
 * regardless of configurations on which the project depends.
 * One of the use cases would be to add a transitive dependency, that is, a dependency of
 * an artifact on which a module depends directly.
 *
 * Each dependency must be specified in the form `"$group:$artifact:$version"`.
 *
 * Kotlin DSL example (`build.gradle.kts`):
 *
 * ```kotlin
 * artifactMeta {
 *     addDependencies(
 *         "com.google.protobuf:protobuf-java:4.13.1",
 *         "org.junit:junit:4.13.2"
 *     )
 *
 *     // OR
 *
 *     explicitDependencies.set(setOf(
 *        "com.google.protobuf:protobuf-java:4.13.1",
 *        "org.junit:junit:4.13.2"
 *     ))
 * }
 * ```
 *
 * Notes:
 * - Explicitly declared dependencies are merged with those discovered from configurations.
 * - Duplicates are de-duplicated, and the list is sorted by the group and artifact ID.
 */
public class ArtifactMetaPlugin : Plugin<Project> {

    /**
     * Applies the plugin to the given project.
     */
    override fun apply(project: Project): Unit = with(project) {
        val outputDir = layout.buildDirectory.dir(WORKING_DIR)

        // Register the extension to configure the plugin behavior.
        val ext = extensions.create(NAME, ArtifactMetaExtension::class.java, this)
        
        // Exclude all `test` configurations by default.
        ext.excludeConfigurations.containing("test")

        val task = tasks.register(TASK_NAME, WriteArtifactMeta::class) { task ->
            task.outputDirectory.convention(outputDir)
        }

        tasks.named("processResources").configure { it.dependsOn(task) }
        afterEvaluate {
            tasks.findByName("sourcesJar")?.dependsOn(task)
        }

        // Add the output directory to the resources
        extensions.getByType<JavaPluginExtension>()
            .sourceSets.getByName("main")
            .resources
            .srcDir(outputDir)
    }

    internal companion object {

        /**
         * The name of the directory under the project `build` where
         * the plugin creates its working files.
         */
        const val WORKING_DIR = "spine"
    }
}
