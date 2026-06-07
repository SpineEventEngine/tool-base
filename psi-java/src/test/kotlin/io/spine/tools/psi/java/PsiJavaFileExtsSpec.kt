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

package io.spine.tools.psi.java

import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`PsiJavaFile` extensions should")
internal class PsiJavaFileExtsSpec : PsiTest() {

    private val file = parse("ClassWithNested.java")

    @Test
    fun `obtain the top-level class`() {
        file.topLevelClass.name shouldBe "ClassWithNested"
    }

    @Test
    fun `throw when the file has no classes`() {
        val emptyFile = PsiFileFactory.getInstance(project).createFileFromText(
            "Empty.java",
            com.intellij.ide.highlighter.JavaFileType.INSTANCE,
            "package org.example;"
        ) as PsiJavaFile
        assertThrows<IllegalStateException> {
            emptyFile.topLevelClass
        }
    }

    @Nested inner class
    `locate a class by simple name` {

        @Test
        fun `returning the top-level class`() {
            val located = file.locate("ClassWithNested")
            located.shouldNotBeNull()
            located.name shouldBe "ClassWithNested"
        }

        @Test
        fun `returning a nested class`() {
            val located = file.locate("ClassWithNested", "InnerTwo", "MyEnum")
            located.shouldNotBeNull()
            located.name shouldBe "MyEnum"
        }

        @Test
        fun `returning 'null' when the top-level class name does not match`() {
            file.locate("NoSuchClass").shouldBeNull()
        }

        @Test
        fun `returning 'null' when a nested class name does not match`() {
            file.locate("ClassWithNested", "NoSuchNested").shouldBeNull()
        }

        @Test
        fun `rejecting an empty list of names`() {
            assertThrows<IllegalArgumentException> {
                file.locate(emptyList())
            }
        }
    }
}
