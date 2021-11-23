/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.project

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.gradle.ProtobufConfigurator
import io.spine.tools.fs.DirectoryName.generated
import io.spine.tools.gradle.ProtobufDependencies.gradlePlugin
import io.spine.tools.resolve
import io.spine.tools.groovy.ConsumerClosure.closure
import java.io.File
import java.nio.file.Path
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * This test suite tests only [Project.generatedDir] extension property.
 *
 * For tests of other `Project` extensions, please see `ProjectExtensionsTest.kt`.
 */
class `'generatedDir' property of 'Project' should` {

    lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir projectPath: Path) {
        project = ProjectBuilder.builder()
            .withName("prj-ext")
            .withProjectDir(projectPath.toFile())
            .build()
        val pluginManager = project.getPluginManager()
        pluginManager.apply(JavaPlugin::class.java)
        pluginManager.apply(gradlePlugin().value())
        project.group = "io.spine.testing"
        project.version = "3.2.1"
    }

    @Test
    fun `use 'generated' under the project dir, if 'protobuf' plugin returns its default value`() {
        assertProperty().isEqualTo(project.projectDir.resolve(generated).toPath())
    }

    @Test
    fun `take user-defined value specified in the 'protobuf' closure`() {
        val customPath = File("${project.projectDir}/protoGenerated").toPath()
        project.protobufConvention.protobuf(closure { protobuf: ProtobufConfigurator ->
            protobuf.generatedFilesBaseDir = customPath.toString()
        })

        assertProperty().isEqualTo(customPath)
    }

    private fun assertProperty() = assertThat(project.generatedDir)
}
