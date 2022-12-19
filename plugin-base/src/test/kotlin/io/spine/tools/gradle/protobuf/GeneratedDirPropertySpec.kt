/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.gradle.protobuf

import io.kotest.matchers.shouldBe
import io.spine.tools.fs.DirectoryName.generated
import io.spine.tools.gradle.protobuf.ProtobufDependencies.gradlePlugin
import io.spine.tools.resolve
import java.io.File
import java.nio.file.Path
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * This test suite tests only [Project.generatedDir] extension property.
 *
 * For tests of other `Project` extensions, please see [ProjectExtensionsSpec].
 *
 * @see [ProjectExtensionsSpec]
 */
@DisplayName("`generatedDir` extension property of `Project` should")
class GeneratedDirPropertySpec {

    lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir projectPath: Path) {
        project = ProjectBuilder.builder()
            .withName("prj-ext")
            .withProjectDir(projectPath.toFile())
            .build()
        with(project) {
            pluginManager.run {
                apply(JavaPlugin::class.java)
                apply(gradlePlugin.id)
            }
            group = "io.spine.testing"
            version = "3.2.1"
        }
    }

    @Test
    fun `use 'generated' under the project dir, if no custom path set for 'protobuf' plugin`() {
        project.generatedDir shouldBe project.projectDir.resolve(generated).toPath()
    }

    @Test
    fun `take user-defined value specified in the 'protobuf' extension`() {
        val customPath = File("${project.projectDir}/protoGenerated").toPath()
        val protobuf = ProtobufGradlePluginAdapter(project)
        protobuf.generatedFilesBaseDir = customPath.toString()

        project.generatedDir shouldBe customPath
    }
}
