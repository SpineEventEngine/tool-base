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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.tools.psi.java.Environment.elementFactory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`CodeBlockAdapter` should")
internal class CodeBlockAdapterSpec : PsiTest() {

    @Test
    fun `wrap the passed PSI code block`() {
        val original = elementFactory.createCodeBlockFromText("{$STATEMENTS_TEXT}", null)
        val adapter = CodeBlockAdapter(original)
        adapter.firstBodyElement.text shouldBe STATEMENT_LINES.first()
        adapter.lastBodyElement.text shouldBe STATEMENT_LINES.last()
    }

    @Test
    fun `be created from text using the factory extension`() {
        val adapter = elementFactory.createCodeBlockAdapterFromText(STATEMENTS_TEXT, null)
        adapter.firstBodyElement.text shouldBe STATEMENT_LINES.first()
        adapter.lastBodyElement.text shouldBe STATEMENT_LINES.last()
    }

    @Test
    fun `when empty, throw if asked for the first or last body element`() {
        val adapter = elementFactory.createCodeBlockAdapterFromText("", null)
        assertThrows<IllegalStateException> {
            adapter.firstBodyElement
        }
        assertThrows<IllegalStateException> {
            adapter.lastBodyElement
        }
    }

    @Test
    fun `append another code block adapter`() {
        val statements = elementFactory.createCodeBlockAdapterFromText(STATEMENTS_TEXT, null)
        val printHello = elementFactory.createCodeBlockAdapterFromText(PRINT_STATEMENT, null)
        statements.append(printHello)
        statements.firstBodyElement.text shouldBe STATEMENT_LINES.first()
        statements.lastBodyElement.text shouldBe PRINT_STATEMENT
    }

    @Test
    fun `prepend another code block adapter`() {
        val statements = elementFactory.createCodeBlockAdapterFromText(STATEMENTS_TEXT, null)
        val printHello = elementFactory.createCodeBlockAdapterFromText(PRINT_STATEMENT, null)
        statements.prepend(printHello)
        statements.firstBodyElement.text shouldBe PRINT_STATEMENT
        statements.lastBodyElement.text shouldBe STATEMENT_LINES.last()
    }

    @Test
    fun `use a hard copy of the underlying code block`() {
        val original = elementFactory.createCodeBlockFromText("{$STATEMENTS_TEXT}", null)
        val adapter = CodeBlockAdapter(original)

        // Make sure the first body elements are the same.
        adapter.firstBodyElement.text shouldBe original.firstBodyElement!!.text

        // Let's remove the first statement from the original block.
        // It shouldn't affect the adapter if the original block was indeed hard-copied.
        original.statements
            .first()
            .delete()

        // Make sure the first body elements are different.
        adapter.firstBodyElement.text shouldNotBe original.firstBodyElement!!.text
    }

    @Nested inner class
    `provide shortcuts for 'PsiElement' to` {

        private val variableStatement = "var value = 0;"
        private val returnStatement = "return null;"
        private val method = elementFactory.createMethodFromText("""
            public Bar foo() {
                $variableStatement
                $returnStatement
            }
        """.trimIndent(), null)
        private val methodBody = method.body!!

        @Test
        fun `add code block adapter`() {
            val adapter = elementFactory.createCodeBlockAdapterFromText(STATEMENTS_TEXT, null)
            methodBody.add(adapter)
            val expected = listOf(variableStatement, returnStatement) + STATEMENT_LINES
            methodBody.statements.map { it.text } shouldBe expected
        }

        @Test
        fun `add code block adapter after the given element`() {
            val adapter = elementFactory.createCodeBlockAdapterFromText(STATEMENTS_TEXT, null)
            val variable = methodBody.getFirstByText(variableStatement)
            methodBody.addAfter(adapter, variable)
            val expected = listOf(variableStatement) + STATEMENT_LINES + listOf(returnStatement)
            methodBody.statements.map { it.text } shouldBe expected
        }

        @Test
        fun `add code block adapter before the given element`() {
            val adapter = elementFactory.createCodeBlockAdapterFromText(STATEMENTS_TEXT, null)
            val variable = methodBody.getFirstByText(variableStatement)
            methodBody.addBefore(adapter, variable)
            val expected = STATEMENT_LINES + listOf(variableStatement, returnStatement)
            methodBody.statements.map { it.text } shouldBe expected
        }
    }
}

private val STATEMENTS_TEXT =
    """
    java.util.Optional<io.spine.validate.ValidationError> error = result.validate();
    var violations = error.get().getConstraintViolationList();
    throw new io.spine.validate.ValidationException(violations);
    """.trimIndent()

private const val PRINT_STATEMENT = "System.out.println(\"Hello World!\");"

private val STATEMENT_LINES = STATEMENTS_TEXT.lines().map { it.trim() }
