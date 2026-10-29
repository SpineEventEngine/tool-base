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

package io.spine.tools.gradle.jvm.plugin

import io.spine.tools.code.SourceSetName.Companion.main
import io.spine.tools.gradle.jvm.plugin.ArtifactMetaExtension.Companion.NAME
import io.spine.tools.gradle.jvm.plugin.WriteArtifactMeta.Companion.TASK_NAME
import io.spine.tools.gradle.task.JavaTaskName.Companion.processResources
import io.spine.tools.gradle.task.JavaTaskName.Companion.sourcesJar
import io.spine.tools.gradle.task.SpineTaskGroup
import io.spine.tools.meta.MavenArtifact
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
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
 * - Duplicates are deduplicated, and the list is sorted by the group and artifact ID.
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
            task.group = SpineTaskGroup.name
            task.description =
                "Writes artifact metadata under `META-INF/io.spine` of the resources"
            task.outputDirectory.convention(outputDir)

            // Snapshot the project state the task needs at configuration time, so that
            // its action does not access `Task.project` during execution. See the
            // `Task.project` deprecation: it is incompatible with the configuration cache.
            task.artifactGroup.set(project.provider { project.group.toString() })
            task.artifactId.set(ext.artifactId)
            task.artifactVersion.set(project.provider { project.version.toString() })
            task.dependencyCoordinates.set(project.provider { collectCoordinates(project, ext) })
        }

        tasks.named(processResources.value()).configure {
            it.dependsOn(task)
        }
        // The `sourcesJar` task may be registered after this plugin is applied,
        // e.g., via `java { withSourcesJar() }`. Unlike `named(String)`, `named(Spec)`
        // includes such a task, and does not create it.
        tasks.named { it == sourcesJar.value() }.configureEach {
            it.dependsOn(task)
        }

        // Add the output directory to the resources
        extensions.getByType<JavaPluginExtension>()
            .sourceSets.getByName(main.value)
            .resources
            .srcDir(outputDir)
    }

    internal companion object {

        /**
         * The ID of this Gradle plugin.
         */
        const val id = "io.spine.artifact-meta"

        /**
         * The path of the directory under the project `build` directory where
         * the plugin creates its working files.
         *
         * The plugin claims its own subdirectory under `build/spine` — the root
         * working directory shared by all Spine plugins — so that the output of
         * the [WriteArtifactMeta] task does not overlap with working directories
         * of other Spine plugins, such as `build/spine/compiler`.
         * Overlapping outputs would force Gradle to require explicit ordering
         * between tasks of otherwise unrelated plugins.
         */
        const val WORKING_DIR = "spine/artifact-meta"
    }
}

/**
 * Collects Maven coordinates of the dependencies to be written into the metadata.
 *
 * Reads the project configurations — honoring the exclusions configured via the
 * [`artifactMeta`][ArtifactMetaExtension] extension — and merges the explicitly declared
 * dependency notations. Invoked from a provider so that the project is read at
 * configuration time (or when the task input is finalized) rather than from the task action.
 */
private fun collectCoordinates(project: Project, ext: ArtifactMetaExtension): Set<String> {
    val excludedByName = ext.excludeConfigurations.named.orNull ?: emptySet()
    val excludedBySubstring = ext.excludeConfigurations.containing.orNull ?: emptySet()

    // Collect from configurations (according to exclusions).
    val coordinates = project.configurations
        .asSequence()
        .filter { it.name !in excludedByName }
        .filter { configuration ->
            val lower = configuration.name.lowercase()
            excludedBySubstring.none { sub -> lower.contains(sub.lowercase()) }
        }
        .flatMap { it.dependencies }
        .mapNotNull { it.toMavenArtifact()?.coordinates }
        .toMutableSet()

    // Add explicitly declared dependencies from the extension.
    coordinates.addAll(ext.explicitDependencies.orNull ?: emptySet())
    return coordinates
}

/**
 * Creates a [MavenArtifact] from a Gradle [Dependency].
 *
 * The `null` checks filter out dependencies that do not have a `group` or a `version`
 * attribute available, which is a safety feature for the dynamic Gradle environment.
 * The dependencies of our interest have the required attributes.
 */
@Suppress("ReturnCount")
private fun Dependency.toMavenArtifact(): MavenArtifact? {
    val group = this.group ?: return null
    val version = this.version ?: return null
    return MavenArtifact(group, this.name, version)
}
