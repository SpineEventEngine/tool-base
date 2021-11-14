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
import java.io.File
import java.nio.file.Path
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class `'ProjectExtensions' should` {

    companion object {

        lateinit var project: Project

        @JvmStatic
        @BeforeAll
        fun setUp(@TempDir projectPath: Path) {
            project = ProjectBuilder.builder()
                .withName("prj-ext")
                .withProjectDir(projectPath.toFile())
                .build()
            project.getPluginManager()
                .apply(JavaPlugin::class.java)
            project.group = "io.spine.testing"
            project.version = "1.2.3"
        }
    }

    @Nested
    inner class `obtain descriptor set file by source set name` {

        @Test
        fun `without 'main' in the file name`() {
            assertName(project.descriptorSetFile(MAIN_SOURCE_SET_NAME))
                .doesNotContain(MAIN_SOURCE_SET_NAME)
        }

        @Test
        fun `with source set name in the file name`() {
            assertName(project.descriptorSetFile(TEST_SOURCE_SET_NAME))
                .contains(TEST_SOURCE_SET_NAME)

            val customSourceSet = "integrationTest"
            assertName(project.descriptorSetFile(customSourceSet))
                .contains(customSourceSet)
        }

        private fun assertName(descriptorSetFile: File) = assertThat(descriptorSetFile.name)
    }

    @Nested
    inner class `obtain artifact by source set name` {

        @Test
        fun main() {
            assertThat(project.artifact(MAIN_SOURCE_SET_NAME))
                .isEqualTo(project.artifact)
        }

        @Test
        fun test() {
            assertThat(project.artifact(TEST_SOURCE_SET_NAME))
                .isEqualTo(project.testArtifact)
        }

        @Test
        fun custom() {
            val customName = "slowTests"
            assertThat(project.artifact(customName).fileSafeId())
                .contains(customName)
        }
    }
}
