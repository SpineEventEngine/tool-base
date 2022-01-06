/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.gragle.testing

import com.google.common.truth.Truth.assertThat
import io.spine.tools.gradle.testing.Replacement
import java.io.File
import java.nio.file.Path
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

class `'Replacement' should` {

    private lateinit var folder: File

    @BeforeEach
    fun setUp(@TempDir tempDir: Path) {
        folder = tempDir.toFile()
    }

    @Test
    fun `not allow empty tokens`() {
        assertThrows<IllegalArgumentException> { Replacement("", "value") }
    }

    @Test
    fun `allow empty values-to-replace-with`() {
        assertThat(Replacement("sometoken", "").value)
            .isEmpty()
    }

    @Nested
    inner class `not accept` {

        @Test
        fun `folder as the 'replaceIn' argument`() {
            assertThrows<IllegalArgumentException> {
                Replacement("some-token", "").replaceIn(folder)
            }
        }

        @Test
        fun `non-existing file as the 'replaceIn' argument`() {
            assertThrows<IllegalArgumentException> {
                Replacement("some-token", "").replaceIn(folder.resolve("foobar"))
            }
        }
    }

    @Nested
    inner class `replace all token occurrences` {

        @Test
        fun `in a file`() {
            val file = folder.resolve("replace_all.test")
            file.writeText(TEXT)
            val value = "replaced"
            Replacement(TOKEN_NAME, value).replaceIn(file)
            file.assertReplaced(TOKEN_NAME, value)
        }

        private fun File.assertReplaced(token: String, value: String) {
            val text = this.readText()
            val expected = TEXT.replace(token, value)
            assertThat(text)
                .isEqualTo(expected)
        }
    }
}

private const val TOKEN_NAME = "@TEST_TOKEN@"
private const val TEXT = "This is a `$TOKEN_NAME` which should be replaced with " +
        "`$TOKEN_NAME`. \n\n\n And this `@TEST@ should remain the same."
