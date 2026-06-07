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

package io.spine.tools.protobuf.gradle

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`SourceSet` extensions for Protobuf should")
internal class SourceSetExtsSpec {

    private lateinit var project: Project
    private lateinit var mainSourceSet: SourceSet

    @BeforeEach
    fun setUp(@TempDir projectPath: Path) {
        project = ProjectBuilder.builder()
            .withProjectDir(projectPath.toFile())
            .build()
        project.pluginManager.apply(JavaPlugin::class.java)
        val javaExt = project.extensions.getByType(JavaPluginExtension::class.java)
        mainSourceSet = javaExt.sourceSets.getByName(MAIN_SOURCE_SET_NAME)
    }

    @Test
    fun `obtain the proto directory set when the Protobuf plugin is applied`() {
        // Applying the protobuf plugin adds the `proto` extension.
        project.plugins.apply(ProtobufDependencies.gradlePlugin.id)

        mainSourceSet.findProtoDirectorySet().shouldNotBeNull()
    }

    @Test
    fun `return 'null' from 'findProtoDirectorySet' when the Protobuf plugin is not applied`() {
        // No `proto` extension is present without the Protobuf Gradle plugin.
        mainSourceSet.findProtoDirectorySet().shouldBeNull()
    }

    @Nested
    inner class `tell whether a source set contains proto files` {

        @Test
        fun `returning 'false' when the Protobuf plugin is not applied`() {
            mainSourceSet.containsProtoFiles() shouldBe false
        }

        @Test
        fun `returning 'false' when the 'proto' set is empty`() {
            project.plugins.apply(ProtobufDependencies.gradlePlugin.id)

            mainSourceSet.containsProtoFiles() shouldBe false
        }

        @Test
        fun `returning 'true' when the 'proto' set has a file`() {
            project.plugins.apply(ProtobufDependencies.gradlePlugin.id)

            val protoSet = mainSourceSet.findProtoDirectorySet()!!
            val protoDir = project.projectDir.resolve("src/main/proto")
            protoDir.mkdirs()
            val protoFile = protoDir.resolve("test.proto")
            protoFile.writeText("syntax = \"proto3\";")
            protoSet.srcDir(protoDir)

            mainSourceSet.containsProtoFiles() shouldBe true
        }
    }
}
