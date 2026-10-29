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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.spine.tools.gradle.testing.Gradle
import io.spine.tools.gradle.testing.Gradle.BUILD_SUCCESSFUL
import io.spine.tools.gradle.testing.runGradleBuild
import io.spine.tools.gradle.testing.under
import io.spine.tools.protobuf.gradle.ProtobufTaskName
import java.io.File
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`GeneratedSourcePlugin` should")
class GeneratedSourcePluginSpec : ProtobufPluginTest() {

    override val group = "test.group"
    override val version = "3.4.5"

    @Test
    fun `copy generated Java sources under project generated directory`() {
        writeSampleProto()
        writeJavaBuildFile()

        // Run the `generateProto` task.
        val task = ProtobufTaskName.generateProto
        val result = runGradleBuild(
            projectDir,
            listOf(task.name()),
            debug = false
        )

        result.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain BUILD_SUCCESSFUL

        // Verify Java sources were copied to `$projectDir/generated/main/java`.
        val generatedJava = projectDir.generatedJava
        val sampleOuter = File(generatedJava, "sample/Sample.java")
        sampleOuter.exists() shouldBe true
    }

    @Test
    fun `register generated Kotlin sources under the 'generatedKotlin' source set`() {
        // The Kotlin `protoc` builtin (enabled by our plugin) generates
        // a Kotlin DSL file for the message.
        writeSampleProto()

        // Build file applying the Kotlin JVM, Protobuf, and our plugins.
        // The `printKotlinSourceDirs` task reports which source set the generated
        // Kotlin directory is registered under.
        Gradle.buildFile.under(projectDir).writeText(
            """
            import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
            import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

            plugins {
                // No version: resolved from the plugin-under-test classpath.
                id("org.jetbrains.kotlin.jvm")
                id("${ProtobufGradlePlugin.id}") version "${ProtobufGradlePlugin.version}"
                id("${GeneratedSourcePlugin.id}")
            }

            group = "$group"
            version = "$version"

            repositories {
                mavenCentral()
            }

            protobuf {
                protoc { artifact = "${ProtobufProtoc.dependency.artifact.coordinates}" }
            }

            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            fun generatedKotlinSrcDirs(set: KotlinSourceSet) =
                set.generatedKotlin.srcDirs

            val main = kotlin.sourceSets.getByName("main")
            tasks.register("printKotlinSourceDirs") {
                doLast {
                    println("KOTLIN_DIRS=" + main.kotlin.srcDirs)
                    println("GENERATED_KOTLIN_DIRS=" + generatedKotlinSrcDirs(main))
                }
            }
            """.trimIndent()
        )

        val generateProto = ProtobufTaskName.generateProto
        val result = runGradleBuild(
            projectDir,
            listOf(generateProto.name(), "printKotlinSourceDirs"),
            debug = false
        )

        result.task(generateProto.path())?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain BUILD_SUCCESSFUL

        // The copied directory is registered under `generatedKotlin`, and is absent
        // from the plain `kotlin` source set. Paths are normalized to forward slashes
        // so the substring check holds on Windows too.
        val output = result.output
        val generatedKotlinLine = output.lineSequence()
            .first { it.startsWith("GENERATED_KOTLIN_DIRS=") }
            .toUnix()
        val kotlinLine = output.lineSequence()
            .first { it.startsWith("KOTLIN_DIRS=") }
            .toUnix()
        val generatedKotlinSubpath = "generated/main/kotlin"
        generatedKotlinLine shouldContain generatedKotlinSubpath
        kotlinLine shouldNotContain generatedKotlinSubpath

        // The generated Kotlin sources are copied under `$projectDir/generated/main/kotlin`.
        val generatedKotlinDir = File(projectDir, "generated/main/kotlin")
        generatedKotlinDir.walkTopDown().any { it.extension == "kt" } shouldBe true
    }

    @Test
    fun `copy generated sources with the configuration cache enabled`() {
        writeSampleProto()
        writeJavaBuildFile()

        // The configuration cache fails the build if a task action invokes `Task.project`,
        // which Gradle deprecates at execution time even with the cache disabled.
        val task = ProtobufTaskName.generateProto
        val arguments = listOf(task.name(), "--configuration-cache")
        val sampleJava = File(generatedJava, "sample/Sample.java")

        val stored = runGradleBuild(projectDir, arguments, debug = false)

        stored.output shouldContain "Configuration cache entry stored"
        stored.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        sampleJava.exists() shouldBe true

        // Make the task run again, so that the copy action is replayed
        // from the deserialized cache entry.
        generatedJava.deleteRecursively() shouldBe true

        val reused = runGradleBuild(projectDir, arguments, debug = false)

        reused.output shouldContain "Configuration cache entry reused"
        reused.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        sampleJava.exists() shouldBe true
    }

    @Test
    fun `copy generated sources with the 'idea' plugin applied`() {
        writeSampleProto()
        // Once the project is evaluated, the Protobuf plugin adds the generated
        // directories to the IDE sources, reading the source directories of
        // the source sets, which this plugin modifies.
        writeJavaBuildFile(idea = true)

        val task = ProtobufTaskName.generateProto
        val result = runGradleBuild(projectDir, listOf(task.name()), debug = false)

        result.task(task.path())?.outcome shouldBe TaskOutcome.SUCCESS
        File(generatedJava, "sample/Sample.java").exists() shouldBe true
    }

    /**
     * Writes the build file applying the `java`, Protobuf, and our plugins.
     *
     * @param idea If `true`, the build also applies the `idea` plugin.
     */
    private fun writeJavaBuildFile(idea: Boolean = false) {
        val ideaPlugin = if (idea) "idea" else ""
        Gradle.buildFile.under(projectDir).writeText(
            """
            plugins {
                id("java")
                id("${ProtobufGradlePlugin.id}") version "${ProtobufGradlePlugin.version}"
                id("${GeneratedSourcePlugin.id}")
                $ideaPlugin
            }

            group = "$group"
            version = "$version"

            repositories {
                mavenCentral()
            }

            protobuf {
                protoc { artifact = "${ProtobufProtoc.dependency.artifact.coordinates}" }
            }
            """.trimIndent()
        )
    }
}

/** Replaces backslashes with forward slashes, normalizing a path across platforms. */
private fun String.toUnix(): String = replace('\\', '/')
