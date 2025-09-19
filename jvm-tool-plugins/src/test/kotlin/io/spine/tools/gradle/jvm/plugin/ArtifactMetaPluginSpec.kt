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

import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.spine.tools.gradle.jvm.plugin.ArtifactMetaPlugin.Companion.WORKING_DIR
import io.spine.tools.gradle.task.BaseTaskName
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
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`ArtifactMetaPlugin` should")
class ArtifactMetaPluginSpec {

    private val pluginClass = ArtifactMetaPlugin::class.java
    private lateinit var project: Project

    @BeforeEach
    fun createProject() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)
    }

    @Test
    fun `register WriteDependencies task when applied`() {
        project.pluginManager.apply(pluginClass)

        val task = project.tasks.findByName(WriteArtifactMeta.TASK_NAME)
        task shouldNotBe null
        (task is WriteArtifactMeta) shouldBe true
    }

    @Test
    fun `make 'processResources' depend on the 'writeDependencies' task`() {
        project.pluginManager.apply(pluginClass)

        val processResources = project.tasks.getByName("processResources")
        val hasDependency = processResources.dependsOn.any {
            dep -> dep.toString().contains(WriteArtifactMeta.TASK_NAME)
        }

        hasDependency shouldBe true
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
                id("io.spine.artifact-meta")
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

        // Create a build file with dependencies.
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
            
            dependencies {
                implementation("com.google.guava:guava:31.1-jre")
                implementation("org.slf4j:slf4j-api:1.7.36")
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
        val resourceDir = File(projectDir, "build/resources/main/$RESOURCE_DIRECTORY")
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
        content shouldContain "maven:com.google.guava:guava:31.1-jre"
        content shouldContain "maven:org.slf4j:slf4j-api:1.7.36"
    }
    
    @Test
    fun `exclude selected configurations when collecting dependencies`(@TempDir projectDir: File) {
        val group = "test.group"
        val version = "1.0.0"
        val artifact = projectDir.name

        // Create a build file with dependencies and exclusion.
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
                exclude.add("implementation")
            }

            dependencies {
                implementation("com.google.guava:guava:31.1-jre")
                implementation("org.slf4j:slf4j-api:1.7.36")
                compileOnly("org.jetbrains:annotations:24.0.1")
            }
            """.trimIndent()
        )

        // Execute the build.
        val task = BaseTaskName.build
        val result = runGradleBuild(projectDir, listOf(task.name))

        // Verify task execution was successful.
        result.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain BUILD_SUCCESSFUL

        // Verify the artifact meta file was created.
        val resourceDir = File(projectDir, "build/resources/main/$RESOURCE_DIRECTORY")
        resourceDir.exists() shouldBe true

        // Read the generated file.
        val metaFiles = resourceDir.listFiles()
        metaFiles shouldNotBe null
        metaFiles!!.size shouldBe 1

        val metaFile = metaFiles[0]
        val content = metaFile.readText()

        // Verify dependencies from the excluded configuration are NOT present.
        content.contains("maven:com.google.guava:guava:31.1-jre").shouldBe(false)
        content.contains("maven:org.slf4j:slf4j-api:1.7.36").shouldBe(false)
        // Dependency from a different configuration may still not be present due to default filtering; this assertion ensures the metadata itself exists.
        content shouldStartWith "maven:$group:$artifact:$version"
    }
}
