/*
 * Copyright 2024, TeamDev. All rights reserved.
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

import io.kotest.matchers.shouldBe
import io.spine.tools.gradle.testing.BuildSrcCopy.Companion.FOLDER_NAME
import io.spine.tools.gradle.testing.BuildSrcCopy.Companion.JAR_NAME
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.div
import kotlin.io.path.exists
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`BuildSrcCopy` should")
class BuildSrcCopySpec {

    private lateinit var buildSrcCopy: BuildSrcCopy

    @field:TempDir
    private lateinit var tempDir: File

    @Test
    fun `copy directories with source code`() {
        buildSrcCopy = BuildSrcCopy(includeBuildSrcJar = false, includeSourceDir = true)
        listOf(
            "aus.weis",
            "src/main/kotlin",
            "src/main/kotlin/force-jacoco.gradle.kts",
            "src/main/groovy/checkstyle.gradle",
            "build.gradle.kts",
            "build.gradle"
        ).forEach(this::assertIsSourceCode)
        
        listOf(
            "build",
            "build/classes",
            ".gradle",
            ".gradle/file-system.probe",
            "buildSrc/.gradle/7.3/executionHistory/executionHistory.lock"
        ).forEach(this::assertIsNotSourceCode)
    }

    @Test
    fun `include 'build' directory if instructed`() {
        buildSrcCopy = BuildSrcCopy(
            includeBuildSrcJar = false,
            includeSourceDir = true,
            includeBuildDir = true
        )

        listOf(
            "build",
            "build/classes",
        ).forEach(this::assertIsSourceCode)

        listOf(
            ".gradle",
            ".gradle/file-system.probe",
            "buildSrc/.gradle/7.3/executionHistory/executionHistory.lock"
        ).forEach(this::assertIsNotSourceCode)
    }

    @Test
    fun `copy 'buildSrc(dot)jar' file by default`() {
        val tool = BuildSrcCopy()
        tool.includeBuildSrcJar shouldBe true

        val jarPath = Paths.get("build/lib/${JAR_NAME}")
        tool.test(jarPath) shouldBe true
    }

    @Test
    fun `not copy 'build' dir by default`() {
        BuildSrcCopy().includeBuildDir shouldBe false
    }

    @Test
    fun `not copy 'src' dir by default`() {
        BuildSrcCopy().includeSourceDir shouldBe false
    }

    @Test
    fun `copy only first-level files of 'buildSrc' files and 'buildSrc(dot)jar' by default`() {
        val target = tempDir.toPath()
        BuildSrcCopy().writeTo(target)

        assertCopied(target, JAR_NAME)

        // We expect at least this file to be present in root `buildSrc` folder.
        assertCopied(target, "build.gradle.kts")
    }

    private fun assertCopied(target: Path, filename: String) {
        val maybeCopy = target / FOLDER_NAME / filename
        assertTrue(maybeCopy.exists()) {
            "`$filename` is expected to be copied by default."
        }
    }

    private fun assertIsSourceCode(path: String) {
        val p = Paths.get(path)
        assertTrue(buildSrcCopy.test(p)) {
            "The path `${p}` is expected to be source code."
        }
    }

    private fun assertIsNotSourceCode(path: String) {
        val p = Paths.get(path)
        assertFalse(buildSrcCopy.test(p)) {
            "The path `${p}` is expected to be NOT source code."
        }
    }
}
