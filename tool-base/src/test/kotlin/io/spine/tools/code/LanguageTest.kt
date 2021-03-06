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

package io.spine.tools.code

import com.google.common.truth.Truth.assertThat
import io.spine.tools.code.CommonLanguages.Java
import io.spine.tools.code.CommonLanguages.JavaScript
import io.spine.tools.code.CommonLanguages.Kotlin
import io.spine.tools.code.CommonLanguages.any
import java.io.File
import java.nio.file.Paths
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class `'Language' should` {

    @Test
    fun `return its name in the string form`() {
        assertThat(Kotlin.toString()).isEqualTo("Kotlin")
    }

    @Test
    fun `expose file pattern`() {
        assertThat(Kotlin.fileExtensions).containsExactly("kt")
        assertThat(Brainfuck().fileExtensions).containsExactly("b", "bf")
    }

    @Test
    fun `format a test line as comments`() {
        assertThat(Java.comment("Hey!")).isEqualTo("// Hey!")
    }

    @Test
    fun `filter files by their extension`() {
        // Passing the directory, rather than file.
        assertThat(JavaScript.matches(File("Windows"))).isFalse()

        // Passing file with extension.
        assertThat(JavaScript.matches(File("safari.swift"))).isFalse()
        
        assertThat(JavaScript.matches(File("mozilla.js"))).isTrue()
    }

    @Test
    fun `throw 'UnsupportedOperationException' for the synthetic 'any' language`() {
        assertThrows<UnsupportedOperationException> { any.comment("kaboom") }
    }

    @Nested
    inner class `support more than one file extension` {

        private val cpp = SlashCommentLanguage("C++", listOf(".HPP", ".cc", ".CPP", "h"))

        @Test
        fun `sorting and lower-casing them on initialization`() {
            assertThat(cpp.fileExtensions).containsExactly("cc", "cpp", "h", "hpp")
        }

        @Test
        fun `matching all kinds of files`() {
            assertMatches("1.cc")
            assertMatches("2.h")
            assertMatches("3.hpp")
            assertMatches("4.cpp")
        }

        private fun assertMatches(file: String) {
            val path = Paths.get(file)
            assertThat(cpp.matches(path)).isTrue()
        }
    }
}

/**
 * [Brainfuck](https://en.wikipedia.org/wiki/Brainfuck) is an esoteric programming language
 * created in 1993 by Urban M??ller.
 */
private class Brainfuck: Language("Brainfuck", listOf(".b", ".bf")) {
    override fun comment(line: String): String = "[ $line ]"
}
