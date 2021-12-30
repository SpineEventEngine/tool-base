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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
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

    @Test
    fun `replace all token occurrences in file`() {
        val file = folder.resolve("replace_all.test")
        val token = "TEST_TOKEN"
        val original = "This is a `@$token@` which should be replaced with " +
                "`@$token@`. And this `@TEST@ should remain the same."
        file.writeText(original)

        val value = "replaced"
        Replacement(token, value).replaceIn(file)

        val actual = file.readText()
        val expected = original.replace("@$token@", value)
        assertThat(actual)
            .isEqualTo(expected)
    }
}
