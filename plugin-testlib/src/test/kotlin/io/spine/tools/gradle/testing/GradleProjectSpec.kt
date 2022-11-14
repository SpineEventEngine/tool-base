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
package io.spine.tools.gradle.testing

import com.google.common.truth.Truth.assertThat
import io.spine.base.Identifier
import io.spine.tools.gradle.task.JavaTaskName.Companion.compileJava
import java.io.File
import java.nio.file.Path
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`GradleProject` should")
class GradleProjectSpec {

    companion object {
        private const val origin = "gradle_project_test"
    }

    private lateinit var projectDir: File
    private lateinit var setup: GradleProjectSetup

    @BeforeEach
    fun setUp(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()
        setup = GradleProject.setupAt(projectDir)
    }

    @Test
    fun `be created with only project directory specified`() {
        val project = setup.create()

        assertThat(project.projectDir)
            .isEqualTo(projectDir)
    }

    @Test
    fun `execute faulty build`() {
        setup.fromResources(origin) { path ->
            val name = path.toString()
            name.contains("Faulty.java") || name.contains("build.gradle")
        }
        val project = setup.create()

        val buildResult = project.executeAndFail(compileJava)
        assertNotNull(buildResult)

        val compileTask = buildResult.task(compileJava.path())
        assertNotNull(compileTask)

        assertThat(compileTask!!.outcome)
            .isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `replace tokens in the build files, but exclude the 'buildSrc' folder`() {
        val replacement = Identifier.newUuid()
        setup.fromResources(origin)
            .replace("TEST_TOKEN", replacement)
            .create()

        val buildScript = projectDir.resolve("build.gradle.kts")
        assertThat(buildScript.readText())
            .contains(replacement)

        val noReplacementFile = projectDir
            .resolve("buildSrc")
            .resolve("no-replacement.txt")

        assertThat(noReplacementFile.readText())
            .doesNotContain(replacement)

        val replacementFile = projectDir
            .resolve("src/main/java/acme/replacement.txt")
        assertThat(replacementFile.readText())
            .contains(replacement)
    }
}
