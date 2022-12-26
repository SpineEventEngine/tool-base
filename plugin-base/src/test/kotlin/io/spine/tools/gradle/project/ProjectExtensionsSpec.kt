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

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.tools.code.SourceSetName
import java.nio.file.Path
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Tests Gradle-related extensions of [org.gradle.api.Project].
 *
 * For tests of Protobuf-specific extensions please see
 * [io.spine.tools.gradle.protobuf.ProjectExtensionsSpec].
 *
 * @see [io.spine.tools.gradle.protobuf.ProjectExtensionsSpec]
 */
@DisplayName("`Project` extensions for Gradle should")
class ProjectExtensionsSpec {

    lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir projectPath: Path) {
        project = ProjectBuilder.builder()
            .withName("gradle-prj-ext")
            .withProjectDir(projectPath.toFile())
            .build()
        with(project) {
            pluginManager.apply(JavaPlugin::class.java)
            group = "io.spine.tests"
            version = "1.2.3"
        }
    }

    @Nested
    @DisplayName("obtain artifact by source set name")
    inner class ObtainingArtifact {

        @Test
        fun main() {
            project.artifact(SourceSetName.main) shouldBe project.artifact
        }

        @Test
        fun test() {
            project.artifact(SourceSetName.test) shouldBe project.testArtifact
        }

        @Test
        fun custom() {
            val customName = SourceSetName("slowTests")

            project.artifact(customName).fileSafeId() shouldContain customName.value
        }
    }

    @Test
    fun `obtain names of source sets`() {
        val sourceSets = project.sourceSets
        val sourceSetNames = project.sourceSetNames

        sourceSetNames shouldHaveSize sourceSets.size
        sourceSetNames shouldContainExactly sourceSets.map { s -> SourceSetName(s.name) }
    }

}
