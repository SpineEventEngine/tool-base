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
import io.spine.tools.gradle.task.JavaTaskName
import io.spine.tools.gradle.testing.runGradleBuild
import io.spine.tools.gradle.testing.under
import java.io.File
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`GeneratedSourcePlugin` should")
class GeneratedSourcePluginSpec {

    private val File.protoDir: File get() = File(this, "src/main/proto")
    private val File.generatedJava: File get() = File(this, "generated/main/java")

    @Test
    fun `copy generated Java sources under project generated directory`(@TempDir projectDir: File) {
        val group = "test.group"
        val version = "1.2.3"

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

        // Build file applying Protobuf plugin and our plugin.
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("com.google.protobuf") version "0.9.5"
                id("io.spine.tools.protobuf.generated-source")
            }

            group = "$group"
            version = "$version"

            repositories {
                mavenCentral()
            }

            protobuf {
                protoc { artifact = "com.google.protobuf:protoc:4.31.1" }
            }
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
        sampleOuter.exists() shouldBe true
    }

    @Test
    fun `make processResources depend on generateProto`(@TempDir projectDir: File) {
        Gradle.settingsFile.under(projectDir).writeText("")

        // Minimal proto to make `generateProto` do some work.
        val protoDir = File(projectDir, "src/main/proto")
        protoDir.mkdirs()
        File(protoDir, "msg.proto").writeText(
            """
            syntax = "proto3";
            package test;
            message M {}
            """.trimIndent()
        )

        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("com.google.protobuf") version "0.9.5"
                id("io.spine.tools.protobuf.generated-source")
            }

            repositories { mavenCentral() }

            protobuf {
                protoc { artifact = "com.google.protobuf:protoc:4.31.1" }
            }
            """.trimIndent()
        )

        // Run `processResources` and ensure `generateProto` was executed (dependency configured).
        val result = runGradleBuild(projectDir, JavaTaskName.processResources)
        result.output shouldContain Gradle.BUILD_SUCCESSFUL

        // Verify Java code was produced, implying `generateProto` ran before `processResources`.
        val generatedJava = projectDir.generatedJava
        val sampleOuter = File(generatedJava, "test/Msg.java")
        
        generatedJava.exists() shouldBe true
        sampleOuter.exists() shouldBe true
    }
}
