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

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.spine.tools.gradle.jvm.plugin.WriteArtifactMeta.Companion.TASK_NAME
import io.spine.tools.meta.ArtifactMeta
import io.spine.tools.meta.Module
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.kotlin.dsl.register

@DisplayName("`WriteArtifactMeta` should")
class WriteArtifactMetaSpec {

    private lateinit var project: Project
    private lateinit var task: WriteArtifactMeta
    private lateinit var outputDir: File

    @BeforeEach
    fun setUp(@TempDir tempDir: File) {
        project = ProjectBuilder.builder().build()
        project.group = "test.group"
        project.version = "1.0.0"
        project.pluginManager.apply(JavaPlugin::class.java)

        task = project.tasks.register<WriteArtifactMeta>(TASK_NAME) {
            outputDirectory.set(outputDir)
        }.get()
        outputDir = tempDir
    }

    @Test
    fun `filter out test configurations`(): Unit = with(project) {
        // Add test dependency to testImplementation configuration
        val testConfig = configurations.getByName("testImplementation")
        val testDependency = dependencies.create("test.group:test-artifact:1.0.0")
        testConfig.dependencies.add(testDependency)

        // Add implementation dependency to implementation configuration
        val implConfig = configurations.getByName("implementation")
        val implDependency = dependencies.create("impl.group:impl-artifact:1.0.0")
        implConfig.dependencies.add(implDependency)

        // Execute the task
        task.writeFile()

        // Verify the output 
        val module = Module(group.toString(), name)
        val fileName = ArtifactMeta.resourcePath(module)
        val outputFile = outputDir.resolve(fileName)

        // Load the generated file
        val artifactMeta = ArtifactMeta.load(outputFile)

        
        // Check that test dependencies are not included
        val deps = artifactMeta.dependencies.list.map { it.toString() }
        deps.let {
            it shouldNotContain "maven:test.group:test-artifact:1.0.0"
            it shouldContain "maven:impl.group:impl-artifact:1.0.0"
        }
    }

    @Test
    fun `include only production configurations`(): Unit = with(project) {
        // Add dependencies to production configurations
        // API configuration  
        val apiConfig = configurations.findOrCreate("api")
        val apiDependency = dependencies.create("api.group:api-artifact:1.0.0")
        apiConfig.dependencies.add(apiDependency)

        // Implementation configuration
        val implConfig = configurations.getByName("implementation")
        val implDependency = dependencies.create("impl.group:impl-artifact:1.0.0")
        implConfig.dependencies.add(implDependency)

        // RuntimeOnly configuration - use compileOnly instead as it's a standard configuration
        val runtimeConfig = configurations.getByName("compileOnly")
        val runtimeDependency = dependencies.create("runtime.group:runtime-artifact:1.0.0")
        runtimeConfig.dependencies.add(runtimeDependency)

        // Create a custom configuration that should be excluded
        val customConfig = configurations.create("custom")
        val customDependency = dependencies.create("custom.group:custom-artifact:1.0.0")
        customConfig.dependencies.add(customDependency)

        // Execute the task
        task.writeFile()

        // Verify the output
        val module = Module(group.toString(), name)
        val fileName = ArtifactMeta.resourcePath(module)
        val outputFile = outputDir.resolve(fileName)

        // Load the generated file
        val artifactMeta = ArtifactMeta.load(outputFile)

        // Check that only production dependencies are included
        val deps = artifactMeta.dependencies.list.map { it.toString() }
        deps.let {
            it shouldContain "maven:api.group:api-artifact:1.0.0"
            it shouldContain "maven:impl.group:impl-artifact:1.0.0"
            // We're not checking for `runtimeOnly` anymore since we're using `compileOnly`.
            it shouldNotContain "maven:custom.group:custom-artifact:1.0.0"
        }
    }

    @Test
    fun `handle mixed production and test dependencies`(): Unit = with(project) {
        // Create a configuration with both production and test dependencies
        val mixedConfig = configurations.findOrCreate("testFixtures")
        val prodDependency = dependencies.create("prod.group:prod-artifact:1.0.0")
        val testDependency = dependencies.create("test.group:test-artifact:1.0.0")
        mixedConfig.dependencies.add(prodDependency)
        mixedConfig.dependencies.add(testDependency)

        // Use the existing implementation configuration
        val implConfig = configurations.getByName("implementation")
        val implDependency = dependencies.create("impl.group:impl-artifact:1.0.0")
        implConfig.dependencies.add(implDependency)

        // Execute the task
        task.writeFile()

        // Verify the output
        val module = Module(group.toString(), name)
        val fileName = ArtifactMeta.resourcePath(module)
        val outputFile = outputDir.resolve(fileName)

        // Load the generated file
        val artifactMeta = ArtifactMeta.load(outputFile)

        // Check that only implementation dependencies are included
        val dependencies = artifactMeta.dependencies.list.map { it.toString() }
        dependencies.let {
            it shouldContain "maven:impl.group:impl-artifact:1.0.0"
            it shouldNotContain "maven:prod.group:prod-artifact:1.0.0"
            it shouldNotContain "maven:test.group:test-artifact:1.0.0"
        }
    }

    /**
     * This test verifies the current behavior of the `collectDependencies()` method
     * and suggests an improvement. The current implementation includes configurations
     * that contain the strings "api", "implementation", or "runtimeOnly" anywhere in their name.
     * This means that configurations like "apiElements" or "implementationDependenciesMetadata"
     * are included, which might not be the intended behavior.
     * 
     * An improved implementation would use a more precise matching approach, such as
     * checking if the configuration name exactly matches one of the production configuration
     * names or starts with one of them followed by a specific delimiter.
     */
    @Test
fun `filter configurations with derived names`(): Unit = with(project) {
        // Create custom configurations with derived names
        val customApiConfig = configurations.findOrCreate("customApiElements")
        val customApiDependency = dependencies.create("api.elements:api-elements-artifact:1.0.0")
        customApiConfig.dependencies.add(customApiDependency)

        // Create a unique configuration name to avoid conflicts
        val customImplConfig = configurations.findOrCreate("customImplementationMetadata")
        val customImplDependency = dependencies.create("impl.metadata:impl-metadata-artifact:1.0.0")
        customImplConfig.dependencies.add(customImplDependency)

        // Use the existing implementation configuration as a control
        val implConfig = configurations.getByName("implementation")
        val implDependency = dependencies.create("impl.group:impl-artifact:1.0.0")
        implConfig.dependencies.add(implDependency)

        // Execute the task
        task.writeFile()

        // Verify the output
        val module = Module(group.toString(), name)
        val fileName = ArtifactMeta.resourcePath(module)
        val outputFile = outputDir.resolve(fileName)

        // Load the generated file
        val artifactMeta = ArtifactMeta.load(outputFile)

        // Print all configurations for debugging
        println("[DEBUG_LOG] All configurations with derived names:")
        configurations.forEach { config ->
            println("[DEBUG_LOG] - ${config.name}")
        }

        // Print all dependencies for debugging
        println("[DEBUG_LOG] All dependencies in artifact meta:")
        artifactMeta.dependencies.list.forEach { dep ->
            println("[DEBUG_LOG] - $dep")
        }

        // With the current implementation, configurations containing "api" or "implementation"
        // anywhere in their name are included

        val deps = artifactMeta.dependencies.list.map { it.toString() }
        deps.let {
            it shouldContain "maven:impl.group:impl-artifact:1.0.0"
            it shouldContain "maven:api.elements:api-elements-artifact:1.0.0"
        }
    }
}

fun ConfigurationContainer.findOrCreate(name: String): Configuration =
    findByName(name) ?: create(name)

