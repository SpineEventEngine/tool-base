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
import io.spine.base.Identifier
import io.spine.tools.gradle.testing.Replacement
import java.io.File
import java.nio.file.Path
import java.security.SecureRandom
import java.util.*
import kotlin.collections.ArrayList
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
    fun `frame the token name with '@' symbols`() {
        val original = "mytoken"
        val actual = Replacement(original, "").token()
        assertThat(actual)
            .isEqualTo("@$original@")
    }

    @Test
    fun `not allow empty tokens`() {
        assertThrows<IllegalArgumentException> { Replacement("", "value")  }
    }

    @Test
    fun `allow empty values-to-replace-with`() {
        assertThat(Replacement("sometoken", "").value)
            .isEmpty()
    }

    @Nested
    inner class `replace all token occurrences` {

        @Test
        fun `in a file`() {
            val ref = TestData(folder, TEXT)
            val value = "replaced"
            Replacement(TOKEN_NAME, value).replaceIn(ref.file)
            ref.assertReplaced(TOKEN_NAME, value)
        }

        @Test
        fun `in files residing in a folder and its subfolders`() {
            val refs = generateFiles(folder)

            val value = "recursively-replaced"
            Replacement(TOKEN_NAME, value)
                .replaceIn(folder)
            for (ref in refs) {
                ref.assertReplaced(TOKEN_NAME, value)
            }
        }

        @Test
        fun `in all files in the folder recursively, excluding some folder`() {
            val toReplace = generateFiles(folder)

            val excludedFolder = folder.resolve("untouchable")
            excludedFolder.mkdirs()
            val toExclude = generateFiles(excludedFolder)

            val value = "recursively-replaced"
            Replacement(TOKEN_NAME, value)
                .replaceIn(folder, excludedFolder)

            for (ref in toReplace) {
                ref.assertReplaced(TOKEN_NAME, value)
            }
            for (ref in toExclude) {
                ref.assertNotReplaced()
            }
        }
    }

    private fun generateFiles(folder: File): ArrayList<TestData> {
        val refs = ArrayList<TestData>()
        for (i in 0..10) {
            refs.add(TestData(folder, TEXT))
        }
        return refs
    }

    companion object {

        private val random: Random = SecureRandom()

        const val TOKEN_NAME = "TEST_TOKEN"
        const val TEXT = "This is a `@$TOKEN_NAME@` which should be replaced with " +
                "`@$TOKEN_NAME@`. \n\n\n And this `@TEST@ should remain the same."

        /**
         * Creates a test file with the specified [content] inside the passed [parentFolder].
         *
         * Prior to creating the file, establishes some number of sub-folders, to which the file
         * is put. The number lies between zero and two.
         *
         * The name of the file and the names of folders are based on UUIDs.
         */
        class TestData(private val parentFolder: File, val content: String) {

            val file: File

            init {
                val randomValue = Identifier.newUuid()
                val nestingLevel = random.nextInt(3)

                var folder = parentFolder
                for(i in 0..nestingLevel) {
                    val subfolder = folder.resolve("nested_${Identifier.newUuid()}")
                    subfolder.mkdirs()
                    folder = subfolder
                }

                file = folder.resolve("replace_all_${randomValue}.test")
                file.writeText(content)
            }

            fun assertReplaced(token: String, value: String) {
                val text = file.readText()
                val expected = content.replace("@$token@", value)
                assertThat(text)
                    .isEqualTo(expected)
            }

            fun assertNotReplaced() {
                val text = file.readText()
                assertThat(text)
                    .isEqualTo(content)
            }
        }
    }
}
