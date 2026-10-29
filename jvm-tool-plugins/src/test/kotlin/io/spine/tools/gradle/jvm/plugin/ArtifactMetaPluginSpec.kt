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

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import io.spine.tools.gradle.jvm.plugin.ArtifactMetaPlugin.Companion.WORKING_DIR
import io.spine.tools.gradle.task.BaseTaskName
import io.spine.tools.gradle.task.JavaTaskName.Companion.processResources
import io.spine.tools.gradle.task.JavaTaskName.Companion.sourcesJar
import io.spine.tools.gradle.task.TaskName
import io.spine.tools.gradle.testing.Gradle
import io.spine.tools.gradle.testing.Gradle.BUILD_SUCCESSFUL
import io.spine.tools.gradle.testing.runGradleBuild
import io.spine.tools.gradle.testing.under
import io.spine.tools.meta.ArtifactMeta
import io.spine.tools.meta.ArtifactMeta.Companion.RESOURCE_DIRECTORY
import io.spine.tools.meta.Module
import java.io.File
import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`ArtifactMetaPlugin` should")
class ArtifactMetaPluginSpec {

    private val pluginClass = ArtifactMetaPlugin::class.java
    private lateinit var project: Project

    /**
     * Stub dependencies to be used in tests.
     */
    private val dependencies = arrayOf(
        "com.google.guava:guava:31.1-jre",
        "org.slf4j:slf4j-api:1.7.36",
        "org.jetbrains:annotations:24.0.1",
        "org.junit.jupiter:junit-jupiter-api:5.12.1"
    )

    @BeforeEach
    fun createProject() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)
    }

    @Test
    fun `register 'WriteArtifactMeta' task when applied`() {
        project.pluginManager.apply(pluginClass)

        val task = project.tasks.findByName(WriteArtifactMeta.TASK_NAME)
        task shouldNotBe null
        (task is WriteArtifactMeta) shouldBe true
    }

    @Test
    fun `make 'processResources' depend on the 'writeArtifactMeta' task`() {
        project.pluginManager.apply(pluginClass)

        val processResources = project.tasks.getByName(processResources.value())
        val hasDependency = processResources.dependsOn.any {
            dep -> dep.toString().contains(WriteArtifactMeta.TASK_NAME)
        }

        hasDependency shouldBe true
    }

    @Test
    fun `make 'sourcesJar' depend on the 'writeArtifactMeta' task without creating it`() {
        project.pluginManager.apply(pluginClass)
        // Registered after the plugin is applied, as a build script does it.
        project.extensions.findByType(JavaPluginExtension::class.java)?.withSourcesJar()
        val created = mutableListOf<String>()
        project.tasks.configureEach { created += it.name }
        // `configureEach` runs at once for the tasks created earlier, so start afresh.
        created.clear()

        (project as DefaultProject).evaluate()

        created shouldNotContain sourcesJar.value()
        val sourcesJar = project.tasks.getByName(sourcesJar.value())
        val dependencies = sourcesJar.taskDependencies.getDependencies(sourcesJar)
        dependencies.map { it.name } shouldContain WriteArtifactMeta.TASK_NAME
    }

    /**
     * Verifies that the file with metadata is created under the `build` directory.
     */
    @Test
    fun `be applied via its ID`(@TempDir projectDir: File) {
        val group = "test.group"
        val artifact = projectDir.name
        val version = "1.0.0"
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("${ArtifactMetaPlugin.id}")
            }

            group = "$group"
            version = "$version"
            """.trimIndent()
        )

        // Execute the build.
        val task = TaskName.of(WriteArtifactMeta.TASK_NAME)
        val result = runGradleBuild(projectDir, task)

        result.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain BUILD_SUCCESSFUL

        val resourcePath = ArtifactMeta.resourcePath(Module(group, artifact))
        val file = File(projectDir, "build/$WORKING_DIR/$resourcePath")
        file.exists() shouldBe true
        val content = file.readText()
        content shouldContain "maven:$group:$artifact:$version"
    }

    /**
     * Verifies that the metadata file got into production resources under `META-INF/io.spine`.
     */
    @Test
    fun `store project dependencies in resources`(@TempDir projectDir: File) {
        val group = "test.group"
        val version = "1.0.0"
        val artifact = projectDir.name

        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("${ArtifactMetaPlugin.id}")
            }

            group = "$group"
            version = "$version"

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation("${dependencies[0]}")
                implementation("${dependencies[1]}")
            }
            """.trimIndent()
        )

        // Execute the build.
        val task = BaseTaskName.build
        val result = runGradleBuild(projectDir, listOf(task.name), debug = true)

        // Verify task execution was successful.
        result.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain BUILD_SUCCESSFUL

        // Verify the artifact meta file was created.
        val resourceDir = resourceDir(projectDir)
        resourceDir.exists() shouldBe true

        // Read the generated file.
        val metaFiles = resourceDir.listFiles()
        metaFiles shouldNotBe null
        metaFiles!!.size shouldBe 1

        val metaFile = metaFiles[0]
        val lines = metaFile.readLines()

        // Verify the content.
        lines.size shouldBeGreaterThan 1
        lines[0] shouldStartWith "maven:$group:$artifact:$version"

        // Verify dependencies are included.
        val content = lines.joinToString("\n")
        content shouldContain dependencies[0]
        content shouldContain dependencies[1]
    }

    @Test
    fun `filter test configurations by default`(@TempDir projectDir: File) {
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("${ArtifactMetaPlugin.id}")
            }

            group = "test.group"
            version = "1.0.0"

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation("${dependencies[0]}")
                implementation("${dependencies[1]}")
                compileOnly("${dependencies[2]}")
                testImplementation("${dependencies[3]}")
            }
            """.trimIndent()
        )

        // Execute the build.
        val task = BaseTaskName.build
        runGradleBuild(projectDir, listOf(task.name))

        // Read the generated file.
        val resourceDir = resourceDir(projectDir)
        val metaFiles = resourceDir.listFiles()
        val metaFile = metaFiles[0]
        val content = metaFile.readText()

        content.let {
            // Verify dependencies from test configurations are NOT present by default.
            it shouldNotContain dependencies[3]

            // Verify that non-test dependencies are present.
            it shouldContain dependencies[0]
            it shouldContain dependencies[1]
            it shouldContain dependencies[2]
        }
    }

    @Test
    fun `exclude selected configurations when collecting dependencies`(@TempDir projectDir: File) {
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("io.spine.artifact-meta")
            }

            group = "test.group"
            version = "1.0.0"

            repositories {
                mavenCentral()
            }

            artifactMeta {
                excludeConfigurations {
                    named("implementation")
                    containing("test")
                }
            }

            dependencies {
                implementation("${dependencies[0]}")
                implementation("${dependencies[1]}")
                compileOnly("${dependencies[2]}")
                testImplementation("${dependencies[3]}")
            }
            """.trimIndent()
        )

        // Execute the build.
        val task = BaseTaskName.build
        runGradleBuild(projectDir, listOf(task.name))

        // Read the generated file.
        val resourceDir = resourceDir(projectDir)
        val metaFiles = resourceDir.listFiles()
        val metaFile = metaFiles[0]
        val content = metaFile.readText()

        content.let {
            // Verify dependencies from the excluded configuration are NOT present.
            it shouldNotContain dependencies[0]
            it shouldNotContain dependencies[1]
            it shouldNotContain dependencies[3]

            // Verify that the dependency from not excluded configuration is present.
            it shouldContain dependencies[2]
        }
    }

    @Nested inner class
    `include explicit dependencies` {

        @Test
        fun `via 'addDependencies' DSL`(@TempDir projectDir: File) {
            includeDependencies(projectDir) { (e1, e2) ->
                "addDependencies(\"$e1\", \"$e2\")"
            }
        }

        @Test
        fun `via 'explicitDependencies' DSL`(@TempDir projectDir: File) {
            includeDependencies(projectDir) { (e1, e2) ->
                "explicitDependencies.set(setOf(\"$e1\", \"$e2\"))"
            }
        }

        fun includeDependencies(
            @TempDir projectDir: File,
            dsl: (Pair<String, String>) -> String
        ) {
            val group = "test.group"
            val version = "1.0.0"
            val explicit1 = "com.google.protobuf:protobuf-java:3.25.5"
            val explicit2 = "org.junit:junit:4.13.2"

            Gradle.buildFile.under(projectDir).writeText(
                """
            plugins {
                id("java")
                id("io.spine.artifact-meta")
            }

            group = "$group"
            version = "$version"

            repositories {
                mavenCentral()
            }

            artifactMeta {
                ${dsl(Pair(explicit1, explicit2))}
            }
            """.trimIndent()
            )

            val task = BaseTaskName.build
            runGradleBuild(projectDir, listOf(task.name))

            val resourceDir = resourceDir(projectDir)
            val metaFiles = resourceDir.listFiles()
            val metaFile = metaFiles[0]
            val content = metaFile.readText()

            content shouldContain explicit1
            content shouldContain explicit2
        }
    }

    @Nested inner class
    `include test configurations when exclusions are cleared` {

        @Test
        fun `via the 'clear' function`(@TempDir projectDir: File) {
            runBuild(projectDir, clearStatement = "clear()")
        }

        @Test
        fun `via setting empty set`(@TempDir projectDir: File) {
            runBuild(projectDir, clearStatement = "containing.set(emptySet())")
        }

        fun runBuild(
            @TempDir projectDir: File,
            clearStatement: String
        ) {
            Gradle.buildFile.under(projectDir).writeText(
                """
                plugins {
                    id("java")
                    id("io.spine.artifact-meta")
                }

                group = "test.group"
                version = "1.0.0"

                repositories {
                    mavenCentral()
                }

                artifactMeta {
                    excludeConfigurations {
                        $clearStatement
                    }
                }

                dependencies {
                    implementation("${dependencies[0]}")
                    implementation("${dependencies[1]}")
                    compileOnly("${dependencies[2]}")
                    testImplementation("${dependencies[3]}")
                }
                """.trimIndent()
            )

            // Execute the build.
            val task = BaseTaskName.build
            runGradleBuild(projectDir, listOf(task.name))

            // Read the generated file.
            val resourceDir = resourceDir(projectDir)
            val metaFiles = resourceDir.listFiles()
            val metaFile = metaFiles[0]
            val content = metaFile.readText()

            content.let {
                // Verify that the test configuration dependency IS present after clearing exclusions.
                it shouldContain dependencies[3]

                // Verify that non-test dependencies are still present.
                it shouldContain dependencies[0]
                it shouldContain dependencies[1]
                it shouldContain dependencies[2]
            }
        }
    }

    @Test
    fun `use explicit artifactId in resource file name`(@TempDir projectDir: File) {
        val group = "test.group"
        val version = "1.0.0"
        val explicitArtifactId = "custom-artifact"

        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("io.spine.artifact-meta")
            }

            group = "$group"
            version = "$version"

            artifactMeta {
                artifactId.set("$explicitArtifactId")
            }
            """.trimIndent()
        )

        val task = TaskName.of(WriteArtifactMeta.TASK_NAME)
        val result = runGradleBuild(projectDir, task)

        result.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain BUILD_SUCCESSFUL

        val resourcePath = ArtifactMeta.resourcePath(Module(group, explicitArtifactId))
        val file = File(projectDir, "build/$WORKING_DIR/$resourcePath")
        file.exists() shouldBe true

        // Verify the contents refer to the custom artifactId, as specified.
        val content = file.readText()
        content shouldContain "maven:$group:$explicitArtifactId:$version"
    }

    /**
     * Obtains the subdirectory under the [projectDir] which contains the artifact meta file.
     */
    private fun resourceDir(projectDir: File): File =
        File(projectDir, "build/resources/main/$RESOURCE_DIRECTORY")
}
