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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.tools.gradle.task.BaseTaskName
import io.spine.tools.gradle.testing.Gradle
import io.spine.tools.gradle.testing.under
import io.spine.tools.gradle.testing.Gradle.BUILD_SUCCESSFUL
import io.spine.tools.gradle.testing.get
import io.spine.tools.gradle.testing.runGradleBuild
import java.io.File
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`SpineSettingsPlugin` should")
internal class SpineSettingsPluginSpec {

    @TempDir
    lateinit var projectDir: File

    /**
     * Creates a settings file applying the [SpineSettingsPlugin] and using
     * the [SpineSettingsExtension] via its DSL.
     */
    @Test
    fun `be applied via its ID`() {
        val settingsFile = Gradle.settingsFile.under(projectDir)
        settingsFile.writeText(
            """
            plugins {
                id("io.spine.settings")
            }
            
            spineSettings {
                versions {
                    base.set("2.0.0")
                }            
            }
            """.trimIndent()
        )

        // Optionally, add an empty build.gradle file.
        Gradle.buildFile.under(projectDir).writeText("")

        // Execute the build.
        val taskName = BaseTaskName.help
        val result = runGradleBuild(projectDir, taskName)

        result[taskName] shouldBe TaskOutcome.SUCCESS
        result.output shouldContain BUILD_SUCCESSFUL
    }
}
