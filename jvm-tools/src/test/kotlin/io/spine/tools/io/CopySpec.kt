/*
 * Copyright 2026, TeamDev. All rights reserved.
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

package io.spine.tools.io

import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.writeText
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`Copy` should")
internal class CopySpec {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var sourceDir: Path
    private lateinit var targetDir: Path

    @BeforeEach
    fun setUp() {
        sourceDir = tempDir.resolve("source").createDirectories()
        targetDir = tempDir.resolve("target").createDirectories()
    }

    @Test
    fun `copy directory and its content`() {
        val file1 = sourceDir.resolve("file1.txt").createFile()
        file1.writeText("content1")
        val subDir = sourceDir.resolve("sub").createDirectories()
        val file2 = subDir.resolve("file2.txt").createFile()
        file2.writeText("content2")

        Copy.copyDir(sourceDir, targetDir)

        val copiedDir = targetDir.resolve(sourceDir.name)
        copiedDir.exists() shouldBe true
        copiedDir.resolve("file1.txt").exists() shouldBe true
        val copiedSubDir = copiedDir.resolve("sub")
        copiedSubDir.exists() shouldBe true
        copiedSubDir.resolve("file2.txt").exists() shouldBe true
    }

    @Test
    fun `copy content only`() {
        sourceDir.resolve("file1.txt").createFile()
        val subDir = sourceDir.resolve("sub").createDirectories()
        subDir.resolve("file2.txt").createFile()

        Copy.copyContent(sourceDir, targetDir)

        targetDir.resolve(sourceDir.name).exists() shouldBe false
        targetDir.resolve("file1.txt").exists() shouldBe true
        targetDir.resolve("sub/file2.txt").exists() shouldBe true
    }

    @Test
    fun `copy content matching a predicate`() {
        sourceDir.resolve("file1.txt").createFile()
        sourceDir.resolve("file2.kt").createFile()

        Copy.copyContent(sourceDir, targetDir) { it.name.endsWith(".kt") }

        targetDir.resolve("file1.txt").exists() shouldBe false
        targetDir.resolve("file2.kt").exists() shouldBe true
    }

    @Test
    fun `create missing nested directories in target`() {
        val nested = sourceDir.resolve("a/b/c").createDirectories()
        nested.resolve("file.txt").createFile()

        Copy.copyContent(sourceDir, targetDir)

        targetDir.resolve("a/b/c/file.txt").exists() shouldBe true
    }

    @Test
    fun `not create directory if it already exists`() {
        val subDir = sourceDir.resolve("sub").createDirectories()
        subDir.resolve("file.txt").createFile()
        val targetSubDir = targetDir.resolve("sub").createDirectories()

        Copy.copyContent(sourceDir, targetDir)

        targetSubDir.exists() shouldBe true
        targetDir.resolve("sub/file.txt").exists() shouldBe true
    }
}
