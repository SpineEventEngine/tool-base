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

@DisplayName("`DescriptorSetFilePlugin` should")
class DescriptorSetFilePluginSpec {

    @Test
    fun `create desc ref and descriptor file for main source set`(@TempDir projectDir: File) {
        val group = "test.group"
        val version = "1.2.3"

        // Settings file (empty is fine for single-project build).
        Gradle.settingsFile.under(projectDir).writeText("")

        // Create a minimal proto file.
        val protoDir = File(projectDir, "src/main/proto")
        protoDir.mkdirs()
        File(protoDir, "sample.proto").writeText(
            """
            syntax = "proto3";
            package sample;
            message Msg {}
            """.trimIndent()
        )

        // Build file applying Protobuf plugin and our plugin.
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("${ProtobufGradlePlugin.id}") version "${ProtobufGradlePlugin.version}"
                id("${DescriptorSetFilePlugin.id}")
            }

            group = "$group"
            version = "$version"

            repositories {
                mavenCentral()
            }

            protobuf {
                protoc { artifact = "${ProtobufProtoc.dependency.artifact.coordinates}" }
            }
            """.trimIndent()
        )

        // Run the generateProto task.
        val task = ProtobufTaskName.generateProto
        val result = runGradleBuild(projectDir, task)
        result.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain Gradle.BUILD_SUCCESSFUL

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
