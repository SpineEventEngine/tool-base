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

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.string.shouldContain
import io.spine.tools.gradle.testing.Gradle
import io.spine.tools.gradle.testing.runGradleBuild
import io.spine.tools.gradle.testing.under
import io.spine.tools.protobuf.gradle.ProtobufDependencies
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Verifies that the plugins of this module make the tasks consuming the output of
 * a `GenerateProtoTask` depend on it without creating these tasks.
 */
@DisplayName("Protobuf setup plugins should")
internal class LazyTaskWiringSpec : ProtobufPluginTest() {

    override val group = "io.spine.tests"
    override val version = "1.0.0"

    @Test
    fun `make 'processResources' depend on 'generateProto' without creating it`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()
        val created = mutableListOf<String>()
        with(project) {
            pluginManager.apply(JavaPlugin::class.java)
            group = this@LazyTaskWiringSpec.group
            version = this@LazyTaskWiringSpec.version
            pluginManager.apply(ProtobufDependencies.gradlePlugin.id)
            pluginManager.apply(DescriptorSetFilePlugin::class.java)
            tasks.configureEach { created += it.name }
        }
        // `configureEach` runs at once for the tasks created earlier, so start afresh.
        created.clear()

        (project as ProjectInternal).evaluate()

        // `ProtobufSetupPlugin` creates the `GenerateProtoTask`s once the project is
        // evaluated, which runs `DescriptorSetFilePlugin.setup()` for them.
        created shouldContainAll listOf("generateProto", "generateTestProto")
        created shouldNotContain "processResources"
        created shouldNotContain "processTestResources"

        val tasks = project.tasks
        tasks.getByName("processResources").dependencies() shouldContain
                tasks.getByName("generateProto")
        tasks.getByName("processTestResources").dependencies() shouldContain
                tasks.getByName("generateTestProto")
    }

    @Test
    fun `create no compilation or resource processing tasks in a build not running them`() {
        writeSampleProto()
        writeKotlinBuildFile(
            """
            tasks.configureEach { println("$CREATED" + name) }
            """
        )

        val result = runGradleBuild(projectDir, listOf("help"), debug = false)

        val created = result.output.lineSequence()
            .filter { it.startsWith(CREATED) }
            .map { it.removePrefix(CREATED) }
            .toList()
        // Proves that the plugins configured the `GenerateProtoTask`s.
        created shouldContain "generateProto"
        created shouldNotContainAnyOf listOf(
            "compileJava", "compileTestJava",
            "compileKotlin", "compileTestKotlin",
            "processResources", "processTestResources",
        )
    }

    @Test
    fun `make Kotlin compilation depend on 'generateProto'`() {
        writeSampleProto()
        writeKotlinBuildFile()

        val result = runGradleBuild(projectDir, listOf("compileKotlin", "--dry-run"))

        result.output shouldContain ":generateProto SKIPPED"
    }

    /**
     * Writes the build file applying the Kotlin JVM, Protobuf, and both plugins
     * of this module, followed by the given [extra] code.
     *
     * The build also applies the `idea` plugin, which makes the Protobuf plugin read
     * the source directories once the project is evaluated.
     */
    private fun writeKotlinBuildFile(extra: String = "") {
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                idea
                // No version: resolved from the plugin-under-test classpath.
                id("org.jetbrains.kotlin.jvm")
                id("${ProtobufGradlePlugin.id}") version "${ProtobufGradlePlugin.version}"
                id("${GeneratedSourcePlugin.id}")
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
            """.trimIndent() + "\n" + extra.trimIndent()
        )
    }

    private companion object {

        /**
         * The prefix of the lines printed by a build for each task it creates.
         */
        const val CREATED = "CREATED_TASK "
    }
}

/**
 * Obtains the tasks this task depends on, resolving the dependencies
 * declared via its inputs, providers, and task collections.
 */
private fun Task.dependencies(): Set<Task> =
    taskDependencies.getDependencies(this)
