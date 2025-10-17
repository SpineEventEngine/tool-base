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

package io.spine.tools.protobuf.gradle.plugin

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.tools.gradle.protobuf.ProtobufTaskName
import io.spine.tools.gradle.testing.Gradle
import io.spine.tools.gradle.testing.runGradleBuild
import io.spine.tools.gradle.testing.under
import java.io.File
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`DescriptorSetFilePlugin` and `GeneratedSourcePlugin` together should")
class PluginsTogetherSpec {

    private val File.protoDir: File get() = File(this, "src/main/proto")
    private val File.generatedJava: File get() = File(this, "generated/main/java")

    private val group = "test.group"
    private val version = "1.2.3"

    @Test
    fun `work when 'GeneratedSourcePlugin' applied before 'DescriptorSetPlugin'`(
        @TempDir projectDir: File
    ) {
        runPlugins(
            projectDir,
            first = GeneratedSourcePlugin.id,
            second = DescriptorSetFilePlugin.id
        )
    }

    @Test
    fun `work when 'DescriptorSetPlugin' applied before 'GeneratedSourcePlugin'`(
        @TempDir projectDir: File
    ) {
        runPlugins(
            projectDir,
            first = DescriptorSetFilePlugin.id,
            second = GeneratedSourcePlugin.id
        )
    }

    /**
     * Runs a project with two Gradle plugins applied.
     *
     * @param projectDir The root directory of the project.
     * @param first The ID of the first plugin to apply.
     * @param second The ID of the second plugin to apply.
     */
    private fun runPlugins(projectDir: File, first: String, second: String) {
        // Settings file (empty is fine for single-project build).
        Gradle.settingsFile.under(projectDir).writeText("")

        // Create a minimal proto file.
        val protoDir = projectDir.protoDir
        protoDir.mkdirs()
        File(protoDir, "sample.proto").writeText(
            """
            syntax = "proto3";
            package sample;
            message Msg {}
            """.trimIndent()
        )

        // Build file applying Protobuf plugin and both Spine plugins in a specific order.
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("${ProtobufGradlePlugin.id}") version "${ProtobufGradlePlugin.version}"
            }

            group = "$group"
            version = "$version"

            repositories {
                mavenCentral()
            }

            protobuf {
                protoc { artifact = "${ProtobufProtoc.dependency.artifact.coordinates}" }
            }

            apply(plugin = "$first")
            apply(plugin = "$second")
            """.trimIndent()
        )

        // Run the generateProto task.
        val task = ProtobufTaskName.generateProto
        val result = runGradleBuild(projectDir, task)
        result.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain Gradle.BUILD_SUCCESSFUL

        // Verify Java sources were copied to `$projectDir/generated/main/java`.
        val generatedJava = projectDir.generatedJava
        val sampleOuter = File(generatedJava, "sample/Sample.java")
        generatedJava.exists() shouldBe true
        sampleOuter.exists() shouldBe true

        // Verify descriptor set file and reference were produced and placed under resources.
        val buildDir = File(projectDir, "build")
        val descriptorsDir = File(buildDir, "descriptors/main")
        val descRef = File(descriptorsDir, "desc.ref")
        descRef.exists() shouldBe true

        val expectedName = "${group}_${projectDir.name}_${version}.desc"
        val descName = descRef.readText().trim()
        descName shouldBe expectedName

        val descriptor = File(descriptorsDir, expectedName)
        descriptor.exists() shouldBe true
    }
}
