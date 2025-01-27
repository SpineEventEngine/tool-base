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

package io.spine.tools.psi.java

import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiTypeElement
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.spine.tools.psi.java.PsiTest.Companion.parse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`PsiElementExts` should")
internal class PsiElementExtsSpec : PsiTest() {

    @Nested
    inner class
    `look for elements by text` {

        @Test
        fun `returning 'null' when not found`() {
            psiClass.findFirstByText("tableAndChair") shouldBe null
        }

        @Test
        fun `throwing when not found`() {
            assertThrows<IllegalArgumentException> {
                psiClass.getFirstByText("tableAndChair")
            }
        }

        @Test
        fun `returning element which is a direct child`() {
            val buildPartial = psiClass.method("buildPartial")
            val returnedType = buildPartial.findFirstByText("io.spine.base.FieldPath")
            returnedType.shouldBeInstanceOf<PsiTypeElement>()
        }

        @Test
        fun `returning element which is an indirect child`() {
            val buildPartial = psiClass.method("buildPartial")
            val statement = buildPartial.findFirstByText("buildPartial0(result);")
            statement.shouldBeInstanceOf<PsiStatement>()
        }

        @Test
        fun `satisfying both 'startsWith' and 'contains' criteria`() {
            val buildPartial = psiClass.methodWithSignature(MERGE_FROM_SIGNATURE)
            val ifStatement = buildPartial.findFirstByText("if (", "parseUnknownField")
            ifStatement.shouldNotBeNull()
            ifStatement.text shouldBe """
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
            """.trim()
        }
    }
}

private val psiFile = parse("FieldPath.java")
private val psiClass = psiFile.topLevelClass.nested("Builder")

private val MERGE_FROM_SIGNATURE = """
        public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
""".trimIndent()
