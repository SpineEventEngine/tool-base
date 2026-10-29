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

package io.spine.tools.protobuf.gradle.plugin

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.tools.gradle.task.JavaTaskName
import io.spine.tools.gradle.testing.Gradle
import io.spine.tools.gradle.testing.Gradle.BUILD_SUCCESSFUL
import io.spine.tools.gradle.testing.runGradleBuild
import io.spine.tools.gradle.testing.under
import io.spine.tools.protobuf.gradle.ProtobufTaskName
import java.io.File
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`DescriptorSetFilePlugin` should")
class DescriptorSetFilePluginSpec : ProtobufPluginTest() {

    override val group = "running.tests"
    override val version = "1.2.3"

    @Test
    fun `create desc ref and descriptor file for main source set`() {
        writeSampleProto()

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

        // Run the `generateProto` task.
        val task = ProtobufTaskName.generateProto
        val result = runGradleBuild(
            projectDir,
            listOf(task.name()),
            debug = false
        )

        result.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain BUILD_SUCCESSFUL

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

    @Test
    fun `create desc ref with the 'idea' plugin applied`() {
        writeSampleProto()

        // Once the project is evaluated, the Protobuf plugin adds the generated
        // directories to the IDE sources, reading the source directories of
        // the source sets, which this plugin modifies.
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("${ProtobufGradlePlugin.id}") version "${ProtobufGradlePlugin.version}"
                id("${DescriptorSetFilePlugin.id}")
                idea
            }

            group = "$group"
            version = "$version"

            repositories { mavenCentral() }

            protobuf {
                protoc { artifact = "${ProtobufProtoc.dependency.artifact.coordinates}" }
            }
            """.trimIndent()
        )

        val task = ProtobufTaskName.generateProto
        val result = runGradleBuild(projectDir, listOf(task.name()), debug = false)

        result.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        File(projectDir, "build/descriptors/main/desc.ref").exists() shouldBe true
    }

    @Test
    fun `make processResources depend on generateProto`() {
        // Minimal proto to make `generateProto` do some work.
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
                id("${ProtobufGradlePlugin.id}") version "${ProtobufGradlePlugin.version}"
                id("${DescriptorSetFilePlugin.id}")
            }

            group = "$group"
            version = "$version"

            repositories { mavenCentral() }

            protobuf {
                protoc { artifact = "${ProtobufProtoc.dependency.artifact.coordinates}" }
            }
            """.trimIndent()
        )

        // Run `processResources` and ensure `generateProto` was executed (dependency configured).
        val result = runGradleBuild(
            projectDir,
            listOf(JavaTaskName.processResources.name()),
            debug = false
        )
        result.output shouldContain BUILD_SUCCESSFUL

        // Verify Java code was produced, implying `generateProto` ran before `processResources`.
        val sampleOuter = File(buildGeneratedJava, "test/Msg.java")

        sampleOuter.exists() shouldBe true
    }
}
