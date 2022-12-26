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
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.spine.tools.code.SourceSetName
import io.spine.tools.code.SourceSetName.Companion.main
import io.spine.tools.code.SourceSetName.Companion.test
import java.nio.file.Path
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Tests for extensions of [Project] related to Protobuf.
 *
 * @see [GeneratedDirPropertySpec]
 */
@DisplayName("`Project` extensions for Protobuf should")
class ProjectExtensionsSpec {

    lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir projectPath: Path) {
        project = ProjectBuilder.builder()
            .withName("protobuf-prj-ext")
            .withProjectDir(projectPath.toFile())
            .build()
        with(project) {
            pluginManager.apply(JavaPlugin::class.java)
            group = "io.spine.tests"
            version = "3.2.1"
        }
    }

    @Nested
    @DisplayName("obtain descriptor set file by source set name")
    inner class ObtainingDescriptorSetFile {

        @Test
        fun `without 'main' in the file name`() {
            project.descriptorSetFile(main).name shouldNotContain MAIN_SOURCE_SET_NAME
        }

        @Test
        fun `with source set name in the file name`() {
            project.descriptorSetFile(test).name shouldContain TEST_SOURCE_SET_NAME

            val customSourceSet = SourceSetName("integrationTest")
            project.descriptorSetFile(customSourceSet).name shouldContain
                    customSourceSet.value
        }
    }


    @Nested
    @DisplayName("obtain `protoDirectorySet`")
    inner class ObtainingProtoDirectorySet {

        @Test
        fun `equal to 'null' if no 'proto' extension added`() {
            project.protoDirectorySet(main) shouldBe null
        }

        @Test
        fun `from the 'proto' extension`() {
            project.plugins.apply(ProtobufDependencies.gradlePlugin.id)

            project.protoDirectorySet(main) shouldNotBe null
        }
    }
}
