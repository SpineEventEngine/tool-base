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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`IoPreconditions` should")
internal class IoPreconditionsSpec {

    @TempDir
    lateinit var tempDir: Path

    @Nested
    @DisplayName("check if file or path exists")
    inner class Exists {

        @Test
        fun `returning the same file if it exists`() {
            val file = tempDir.resolve("exists.txt").createFile().toFile()
            IoPreconditions.checkExists(file) shouldBe file
        }

        @Test
        fun `throwing 'IllegalStateException' if the file is missing`() {
            val file = tempDir.resolve("missing.txt").toFile()
            shouldThrow<IllegalStateException> {
                IoPreconditions.checkExists(file)
            }
        }

        @Test
        fun `returning the same path if it exists`() {
            val path = tempDir.resolve("exists_path").createDirectories()
            IoPreconditions.checkExists(path) shouldBe path
        }

        @Test
        fun `throwing 'IllegalStateException' if the path is missing`() {
            val path = tempDir.resolve("missing_path")
            shouldThrow<IllegalStateException> {
                IoPreconditions.checkExists(path)
            }
        }
    }

    @Nested
    @DisplayName("check if the path is a directory")
    inner class IsDirectory {

        @Test
        fun `returning the path if it is a directory`() {
            val dir = tempDir.resolve("dir").createDirectories()
            IoPreconditions.checkIsDirectory(dir) shouldBe dir
        }

        @Test
        fun `throwing 'IllegalArgumentException' if the path is a file`() {
            val file = tempDir.resolve("not_a_dir").createFile()
            shouldThrow<IllegalArgumentException> {
                IoPreconditions.checkIsDirectory(file)
            }
        }
    }

    @Nested
    @DisplayName("check that the file is not a directory")
    inner class NotDirectory {

        @Test
        fun `returning the file if it is not a directory`() {
            val file = tempDir.resolve("file").createFile().toFile()
            IoPreconditions.checkNotDirectory(file) shouldBe file
        }

        @Test
        fun `throwing 'IllegalStateException' if the file is a directory`() {
            val dir = tempDir.resolve("actual_dir").createDirectories().toFile()
            shouldThrow<IllegalStateException> {
                IoPreconditions.checkNotDirectory(dir)
            }
        }

        @Test
        fun `returning the file if it does not exist`() {
            val file = tempDir.resolve("non_existent_file").toFile()
            IoPreconditions.checkNotDirectory(file) shouldBe file
        }
    }
}
