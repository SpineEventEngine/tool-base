/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.tools.psi.java

import com.intellij.psi.PsiJavaFile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Parsing of nested classes should")
class NestedParsingSpec: ParsingTest() {

    companion object {

        const val topLevelClass = "ClassWithNested"

        lateinit var file: PsiJavaFile

        @BeforeAll
        @JvmStatic
        fun parseFile() {
            val code = readResource("$topLevelClass.java")
            file = parser.parse(code)
        }
    }

    @Test
    fun `locate top level class`() {
        file.locate(topLevelClass) shouldNotBe null
    }

    @Nested inner class
    `locate nested` {

        private fun assertFound(vararg simpleName: String) {
            val inner = file.locate(simpleName.asIterable())
            inner shouldNotBe null
            inner?.lineNumber shouldNotBe -1
        }

        @Test
        fun `non-static class`() =
            assertFound(topLevelClass, "InterimInner")

        @Test
        fun `static class`() =
            assertFound(topLevelClass, "InnerTwo")

        @Test
        fun `enum under a nested class`() =
            assertFound(topLevelClass, "InnerTwo", "MyEnum")
    }


    @Nested inner class
    `detect missing` {

        private fun assertNotFound(vararg simpleName: String) {
            file.locate(simpleName.asIterable()) shouldBe null
        }

        @Test
        fun `top level class`() =
            assertNotFound("MissingTop", "InnerTwo")

        @Test
        fun `nested class`() =
            assertNotFound(topLevelClass, "MyEnum")
    }
}
