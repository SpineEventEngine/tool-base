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

package io.spine.tools.gradle.project

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.tools.code.Java
import io.spine.tools.code.Kotlin
import io.spine.tools.code.SourceSetName
import io.spine.tools.code.SourceSetName.Companion.main
import io.spine.tools.code.SourceSetName.Companion.test
import io.spine.tools.gradle.JavaConfigurationName
import io.spine.tools.gradle.named
import io.spine.tools.gradle.task.findKotlinDirectorySet
import io.spine.tools.meta.MavenArtifact
import java.nio.file.Path
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
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
 * [io.spine.tools.gradle.protobuf.ProjectExtsSpec].
 *
 * @see [io.spine.tools.gradle.protobuf.ProjectExtsSpec]
 */
@DisplayName("`Project` extensions for Gradle should")
class ProjectExtsSpec {

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

    @Test
    fun `obtain names of source sets`() {
        val sourceSets = project.sourceSets
        val sourceSetNames = project.sourceSetNames

        sourceSetNames shouldHaveSize sourceSets.size
        sourceSetNames shouldContainExactly sourceSets.map { s -> SourceSetName(s.name) }
    }

    @Test
    fun `tell that the project can deal with Java but not Kotlin`() {
        project.let {
            it.hasJava() shouldBe true
            it.hasKotlin() shouldBe false
            it.hasJavaOrKotlin() shouldBe true
        }
    }

    @Nested inner class
    `tell if there is a compile task for a language` {

        @Test
        fun `detecting a lazily registered task`() {
            project.tasks.register("compileKotlin")

            project.hasKotlin() shouldBe true
            project.hasCompileTask(Kotlin) shouldBe true
            // The `java` plugin registers `compileJava` lazily too.
            project.hasCompileTask(Java) shouldBe true
        }

        @Test
        fun `detecting a task of a custom source set`() {
            project.tasks.register("compileIntegrationTestKotlin")

            project.hasKotlin() shouldBe true
        }

        @Test
        fun `ignoring a task which does not follow the naming pattern`() {
            project.tasks.register("kotlinCompile")

            project.hasKotlin() shouldBe false
        }

        @Test
        fun `without realizing tasks`() {
            var compileKotlinConfigured = false
            project.tasks.register("compileKotlin") { compileKotlinConfigured = true }
            val realized = mutableListOf<String>()
            project.tasks.configureEach { realized += it.name }
            // `configureEach` runs at once for the tasks created earlier, so start afresh.
            realized.clear()

            project.hasKotlin() shouldBe true
            project.hasCompileTask(Java) shouldBe true

            compileKotlinConfigured shouldBe false
            realized.shouldBeEmpty()
        }
    }

    @Test
    fun `obtain a source set by its name`() {
        project.let {
            it.sourceSet("main").name shouldBe "main"
            it.sourceSet(main).name shouldBe "main"
        }
    }

    @Test
    fun `obtain an artifact for a source set`() {
        project.let {
            it.artifact(main) shouldBe
                    MavenArtifact("io.spine.tests", "gradle-prj-ext", "1.2.3")
            it.artifact(test) shouldBe
                    MavenArtifact("io.spine.tests", "gradle-prj-ext", "1.2.3", "test")
        }
    }

    @Test
    fun `obtain a configuration by its name`() {
        project.let {
            it.configuration("implementation").name shouldBe "implementation"
            it.configuration(JavaConfigurationName.implementation).name shouldBe "implementation"
        }
    }

    @Test
    fun `find compile tasks for a source set`() {
        val mainSet = project.sourceSet(main)

        project.let {
            it.findJavaCompileFor(mainSet) shouldNotBe null
            // No Kotlin plugin is applied, so there is no Kotlin compile task.
            it.findKotlinCompileFor(mainSet) shouldBe null
        }
    }

    @Test
    fun `expose source set extensions`() {
        val mainSet = project.sourceSet(main)

        mainSet.let {
            it.named shouldBe main
            // No Kotlin extension is present in a plain Java project.
            it.findKotlinDirectorySet() shouldBe null
        }
    }

    @Test
    fun `use the Maven publication 'artifactId' for the artifact name`() {
        project.pluginManager.apply(MavenPublishPlugin::class.java)
        val publishing = project.extensions.getByType(PublishingExtension::class.java)
        publishing.publications.create("maven", MavenPublication::class.java) {
            it.artifactId = "custom-artifact-id"
        }

        project.artifact(main).name shouldBe "custom-artifact-id"
    }
}
