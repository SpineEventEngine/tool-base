/*
 * Copyright 2024, TeamDev. All rights reserved.
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

package io.spine.tools.code

import com.google.common.truth.Truth.assertThat
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

/**
 * This test suite verifies that the Kotlin API for [Language] works as expected.
 *
 * For Java-related part of the API, see [LanguageJavaApiSpec].
 */
@DisplayName("`Language` should")
class LanguageKotlinApiSpec {

    @Test
    fun `return its name in the string form`() {
        Kotlin.toString() shouldBe "Kotlin"
    }

    @Test
    fun `expose file pattern`() {
        Kotlin.fileExtensions shouldContainExactly listOf("kt")

        Brainfuck.fileExtensions shouldContainExactly listOf("b", "bf")
    }

    @Test
    fun `format a test line as comments`() {
        Java.comment("Hey!") shouldBe "/* Hey! */"
    }

    @Test
    fun `filter files by their extension`() {
        // Passing the directory, rather than a file.
        Protobuf.matches(File("Windows")) shouldBe false

        // Passing file with an extension.
        TypeScript.matches(File("safari.swift")) shouldBe false

        JavaScript.matches(File("mozilla.js")) shouldBe true
    }

    @Test
    fun `throw 'UnsupportedOperationException' for the synthetic 'any' language`() {
        assertThrows<UnsupportedOperationException> { AnyLanguage.comment("kaboom") }
    }

    @Nested
    inner class `support more than one file extension` {

        private val cpp =
            object : SlashAsteriskCommentLang("C++", listOf(".HPP", ".cc", ".CPP", "h")) {}

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
            cpp.matches(path) shouldBe true
        }
    }

    @Test
    fun `'TypeScript' should recognise all the files`() {
        with(TypeScript) {
            matches(File("foo.ts")) shouldBe true
            matches(File("jQuery.d.ts")) shouldBe true
            matches(File("jQuery.js")) shouldBe true
            matches(File("bar.map")) shouldBe true
            matches(File("Main.java")) shouldBe false
        }
    }

    @Nested inner class
    `obtain a 'Language' by file extension` {

        @Test
        fun `for known languages`() {
            Language.of(File("File.java")) shouldBe Java
            Language.of(File("File.kt")) shouldBe Kotlin
            Language.of(File("file.js")) shouldBe JavaScript
            Language.of(File("file.ts")) shouldBe TypeScript
            Language.of(File("vader.dart")) shouldBe Dart
            Language.of(File("file.proto")) shouldBe Protobuf
        }

        @Test
        fun `returning 'AnyLanguage' for unsupported languages`() {
            Language.of(Path("main.cpp")) shouldBe AnyLanguage
            Language.of(Path("file.h")) shouldBe AnyLanguage
            Language.of(Path("file.")) shouldBe AnyLanguage
            Language.of(Path("file")) shouldBe AnyLanguage
        }

        @Test
        fun `disallowing passing a directory`(@TempDir dir: Path) {
            assertThrows<IllegalArgumentException> {
                Language.of(dir)
            }
            assertThrows<IllegalArgumentException> {
                Language.of(dir.toFile())
            }
        }
    }
}

/**
 * [Brainfuck](https://en.wikipedia.org/wiki/Brainfuck) is an esoteric programming language
 * created in 1993 by Urban Müller.
 */
private object Brainfuck: Language("Brainfuck", listOf(".b", ".bf")) {
    override fun comment(line: String): String = "[ $line ]"
}
