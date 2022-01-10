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

package io.spine.tools.gradle.project

import com.google.common.truth.Truth.assertThat
import io.spine.tools.gradle.ProtobufDependencies
import io.spine.tools.code.SourceSetName
import io.spine.tools.code.SourceSetName.Companion.main
import io.spine.tools.code.SourceSetName.Companion.test
import java.io.File
import java.nio.file.Path
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class `'ProjectExtensions' should` {

    lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir projectPath: Path) {
        project = ProjectBuilder.builder()
            .withName("prj-ext")
            .withProjectDir(projectPath.toFile())
            .build()
        with(project) {
            pluginManager.apply(JavaPlugin::class.java)
            group = "io.spine.testing"
            version = "1.2.3"
        }
    }

    @Nested
    inner class `obtain descriptor set file by source set name` {

        @Test
        fun `without 'main' in the file name`() {
            assertName(project.descriptorSetFile(main))
                .doesNotContain(MAIN_SOURCE_SET_NAME)
        }

        @Test
        fun `with source set name in the file name`() {
            assertName(project.descriptorSetFile(test))
                .contains(TEST_SOURCE_SET_NAME)

            val customSourceSet = SourceSetName("integrationTest")
            assertName(project.descriptorSetFile(customSourceSet))
                .contains(customSourceSet.value)
        }

        private fun assertName(descriptorSetFile: File) = assertThat(descriptorSetFile.name)
    }

    @Nested
    inner class `obtain artifact by source set name` {

        @Test
        fun main() {
            assertThat(project.artifact(main))
                .isEqualTo(project.artifact)
        }

        @Test
        fun test() {
            assertThat(project.artifact(test))
                .isEqualTo(project.testArtifact)
        }

        @Test
        fun custom() {
            val customName = SourceSetName("slowTests")
            assertThat(project.artifact(customName).fileSafeId())
                .contains(customName.value)
        }
    }

    @Test
    fun `obtain names of source sets`() {
        val sourceSets = project.sourceSets
        val sourceSetNames = project.sourceSetNames

        val assertNames = assertThat(sourceSetNames)
        assertNames.hasSize(sourceSets.size)
        assertNames.containsExactlyElementsIn(sourceSets.map { s -> SourceSetName(s.name) })
    }

    @Nested
    inner class `obtain 'protoDirectorySet'` {

        @Test
        fun `equal to 'null' if no 'proto' extension added`() {

            assertDirectorySet().isNull()
        }

        @Test
        fun `from the 'proto' extension`() {
            project.plugins.apply(ProtobufDependencies.gradlePlugin.id)

            assertDirectorySet().isNotNull()
        }

        private fun assertDirectorySet() = assertThat(project.protoDirectorySet(main))
    }
}
