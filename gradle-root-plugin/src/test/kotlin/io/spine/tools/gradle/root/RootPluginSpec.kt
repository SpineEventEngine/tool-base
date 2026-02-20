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

package io.spine.tools.gradle.root

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.spine.tools.gradle.task.BaseTaskName
import io.spine.tools.gradle.testing.Gradle
import io.spine.tools.gradle.testing.Gradle.BUILD_SUCCESSFUL
import io.spine.tools.gradle.testing.runGradleBuild
import io.spine.tools.gradle.testing.under
import java.io.File
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir

@DisplayName("`RootPlugin` should")
internal class RootPluginSpec {

    private val pluginClass = RootPlugin::class.java

    private lateinit var project: Project

    @BeforeEach
    fun createProject() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    fun `create extension when applied`() {
        project.pluginManager.apply(pluginClass)
        project.extensions.run {
            val ext = findByName(RootExtension.NAME)
            ext shouldNotBe null
            findByType(RootExtension::class.java) shouldBe ext
        }
    }

    @Test
    fun `do not throw when applied twice`() {
        assertDoesNotThrow {
            project.pluginManager.run {
                apply(pluginClass)
                apply(pluginClass)
            }
        }
    }

    @Test
    fun `apply standard repositories`() {
        project.pluginManager.apply(pluginClass)

        val repositories = project.repositories.map { it.name }
        repositories shouldContain "MavenLocal"
        repositories shouldContain "MavenRepo"

        val mavenRepositories = project.repositories.withType(MavenArtifactRepository::class.java)
        val urls = mavenRepositories.map { it.url.toString() }

        urls shouldContain "https://europe-maven.pkg.dev/spine-event-engine/releases"
        urls shouldContain "https://europe-maven.pkg.dev/spine-event-engine/snapshots"
    }

    @Test
    fun `apply repositories if 'repositoriesMode' is 'PREFER_PROJECT'`(
        @TempDir projectDir: File
    ) {
        Gradle.settingsFile.under(projectDir).writeText(
            """
            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
            }
            """.trimIndent()
        )
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("io.spine.root")
            }
            
            tasks.register("checkRepos") {
                doLast {
                    if (project.repositories.isEmpty()) {
                        println("NO_REPOS")
                    } else {
                        project.repositories.forEach { 
                            println("REPO: " + it.name) 
                        }
                    }
                }
            }
            """.trimIndent()
        )

        val result = runGradleBuild(projectDir, listOf("checkRepos"))
        result.output shouldContain "REPO: MavenLocal"
    }

    @Test
    fun `not apply repositories if 'repositoriesMode' is other than 'PREFER_PROJECT'`(
        @TempDir projectDir: File
    ) {
        Gradle.settingsFile.under(projectDir).writeText(
            """
            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
            }
            """.trimIndent()
        )
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("io.spine.root")
            }
            
            tasks.register("checkRepos") {
                doLast {
                    if (project.repositories.isEmpty()) {
                        println("NO_REPOS")
                    } else {
                        project.repositories.forEach { 
                            println("REPO: " + it.name) 
                        }
                    }
                }
            }
            """.trimIndent()
        )

        val result = runGradleBuild(projectDir, listOf("checkRepos"))
        result.output shouldContain "NO_REPOS"
        result.output shouldNotContain "REPO: MavenLocal"
    }

    @Test
    fun `be applied via its ID`(@TempDir projectDir: File) {
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("io.spine.root")
            }
            
            spine {
                // Nothing here so far.
            }
            """.trimIndent())

        // Execute the build.
        val taskName = BaseTaskName.help
        val result = runGradleBuild(projectDir, taskName)

        result.task(":$taskName")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain BUILD_SUCCESSFUL
    }
}
