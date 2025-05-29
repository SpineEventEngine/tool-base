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

package io.spine.tools.plugin

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

@DisplayName("`WorkingDirectory` should")
class WorkingDirectorySpec {

    private val errorMessageStart = "Unable to resolve the subdirectory"

    @Test
    fun `initialize a valid working directory`(@TempDir parent: Path) {
        val name = "validSubDir"

        val workingDirectory = WorkingDirectory(parent, name)

        workingDirectory.path shouldBe parent.resolve(name)
        workingDirectory.exists shouldBe false
    }

    @Test
    fun `fail initialization with blank directory name`(@TempDir parent: Path) {
        val name = "  "
        val exception = assertThrows<IllegalArgumentException> {
            WorkingDirectory(parent, name)
        }
        exception.message shouldContain errorMessageStart
    }

    @Test
    fun `fail initialization with invalid path`(@TempDir parent: Path) {
        val name = "invalid/name\\with:chars"

        val exception = assertThrows<IllegalArgumentException> {
            WorkingDirectory(parent, name)
        }
        exception.message shouldContain errorMessageStart
    }

    @Test
    fun `fail initialization if name resolves to the parent directory`(@TempDir parent: Path) {
        val name = ".."

        val exception = assertThrows<IllegalArgumentException> {
            WorkingDirectory(parent, name)
        }
        exception.message shouldContain errorMessageStart
    }

    @Test
    fun `return false for exists when directory does not exist`(@TempDir parent: Path) {
        val name = "nonExistentDir"
        val workingDirectory = WorkingDirectory(parent, name)

        workingDirectory.exists shouldBe false
    }

    @Test
    fun `return 'true' for exists when directory already exists`(@TempDir parent: Path) {
        val name = "existingDir"
        val path = parent.resolve(name)
        Files.createDirectory(path)
        val workingDirectory = WorkingDirectory(parent, name)

        workingDirectory.exists shouldBe true
    }

    @Test
    fun `create directory and return 'true' when it does not exist`(@TempDir parent: Path) {
        val name = "newDir"
        val workingDirectory = WorkingDirectory(parent, name)

        val created = workingDirectory.create()

        created shouldBe true
        workingDirectory.path.exists() shouldBe true
        workingDirectory.exists shouldBe true
    }

    @Test
    fun `return 'true' when 'create' is called on existing directory`(@TempDir parent: Path) {
        val name = "existingDir"
        val path = parent.resolve(name)
        Files.createDirectory(path)
        val workingDirectory = WorkingDirectory(parent, name)

        val created = workingDirectory.create()

        created shouldBe true
        workingDirectory.exists shouldBe true
    }
}
