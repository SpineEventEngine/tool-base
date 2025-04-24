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

package io.spine.tools.gradle.lib

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.tools.gradle.lib.given.StubSettingsPlugin
import io.spine.tools.gradle.task.BaseTaskName
import io.spine.tools.gradle.testing.Gradle
import io.spine.tools.gradle.testing.Gradle.BUILD_SUCCESSFUL
import io.spine.tools.gradle.testing.GradleProject
import io.spine.tools.java.classpathElement
import io.spine.tools.gradle.testing.get
import io.spine.tools.gradle.testing.under
import java.io.File
import org.gradle.api.Plugin
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * The ID of the plugin under the test.
 *
 * The value of this constant must match the name of the `.properties` file under
 * `resources/META-INF/gradle-plugins/` directory.
 */
private const val PLUGIN_ID = """io.spine.test.settings"""

@DisplayName("`LibrarySettingsPlugin` should")
internal class LibrarySettingsPluginSpec {

    @TempDir
    lateinit var projectDir: File

    @Test
    fun `add custom extension`() {
        @Language("Groovy") // It's Kotlin, actually, but highlighting works better for Groovy.
        val text = """
            plugins {
                id("$PLUGIN_ID")
            }                        
            
            spineSettings {
                // This is the extension added by the plugin under the test.
                nameHolder.name.set("test-name")            
            }
            """.trimIndent()
        Gradle.settingsFile.under(projectDir).writeText(text)

        // Add the plugin class and its definition to the `GradleRunner` classpath.
        // By default, the runner picks only the `main` source set.
        @Suppress("UNCHECKED_CAST")
        val pluginClass = StubSettingsPlugin::class.java as Class<Plugin<*>>
        val testFixturesFile = pluginClass.classpathElement()

        val gradleProject = GradleProject.setupAt(projectDir)
            .withPluginClasspath(testFixturesFile)
            .create()

        // Execute the build.
        val taskName = BaseTaskName.help
        val result = gradleProject.executeTask(taskName)

        result[taskName] shouldBe TaskOutcome.SUCCESS
        result.output shouldContain BUILD_SUCCESSFUL
    }
}
