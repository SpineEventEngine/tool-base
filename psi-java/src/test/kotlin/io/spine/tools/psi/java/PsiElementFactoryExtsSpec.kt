/*
 * Copyright 2024, TeamDev. All rights reserved.
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

import com.intellij.psi.javadoc.PsiDocComment
import io.kotest.matchers.shouldBe
import io.spine.testing.TestValues.randomString
import io.spine.tools.psi.java.Environment.elementFactory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`PsiElementFactory` extensions should")
internal class PsiElementFactoryExtsSpec {

    @Nested inner class
    `create one-line Javadoc` {

        @Test
        fun `with the given content`() {
            val line = randomString()
            val javadoc = elementFactory.createJavadoc(line)
            javadoc.text shouldBe "/** $line */"
        }

        @Test
        fun `prohibiting empty Javadoc`() {
            assertThrows<IllegalArgumentException> {
                elementFactory.createJavadoc("")
            }
        }

        /**
         * We do not allow multi-line Javadoc comments passed to the utility under the tests
         * to encourage using [com.intellij.psi.PsiElementFactory.createDocCommentFromText]
         * method directly when more advanced Javadoc is needed.
         *
         * We do this to avoid formatting and other possible issues which could occur, had
         * we allowed multi-line comments without surrounding slashes and star symbols.
         */
        @Test
        fun `prohibiting multi-line Javadoc`() {
            assertThrows<IllegalArgumentException> {
                elementFactory.createJavadoc("line1.\nline2.")
            }
        }
    }

    @Nested inner class
    `create private constructor` {

        private val className = "StubClass"
        private val cls = elementFactory.createClass(className)

        @Test
        fun `for the given class`() {
            val ctor = elementFactory.createPrivateConstructor(cls)

            ctor.name shouldBe className
            ctor.isPrivate shouldBe true
        }

        @Test
        fun `with Javadoc one-line comment`() {
            val line = randomString()

            val ctor = elementFactory.createPrivateConstructor(cls, line)
            val javadoc = ctor.children.first()
            (javadoc is PsiDocComment) shouldBe true
            javadoc.text shouldBe "/** $line */"
        }

        @Test
        fun `prohibiting empty Javadoc`() {
            assertThrows<IllegalArgumentException> {
                elementFactory.createPrivateConstructor(cls, "")
            }
        }

        @Test
        fun `prohibiting multi-line Javadoc`() {
            assertThrows<IllegalArgumentException> {
                elementFactory.createPrivateConstructor(cls, "line1.\nline2.")
            }
        }
    }
}
