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
import io.spine.tools.psi.java.Environment.elementFactory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`PsiStatements` should")
internal class PsiStatementsSpec : PsiTest() {

    @Test
    fun `be created from a code block`() {
        val codeBlock = elementFactory.createCodeBlockFromText("{$STATEMENTS_TEXT}", null)
        val statements = PsiStatements(codeBlock)
        statements.firstChild.text shouldBe STATEMENT_LINES.first()
        statements.lastChild.text shouldBe STATEMENT_LINES.last()
    }

    @Test
    fun `be created from text using the factory`() {
        val statements = elementFactory.createStatementsFromText(STATEMENTS_TEXT, null)
        statements.firstChild.text shouldBe STATEMENT_LINES.first()
        statements.lastChild.text shouldBe STATEMENT_LINES.last()
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
        fun `add statements`() {
            val statements = elementFactory.createStatementsFromText(STATEMENTS_TEXT, null)
            methodBody.add(statements)
            val expected = listOf(variableStatement, returnStatement) + STATEMENT_LINES
            methodBody.statements.map { it.text } shouldBe expected
        }

        @Test
        fun `add statements after the given element`() {
            val statements = elementFactory.createStatementsFromText(STATEMENTS_TEXT, null)
            val variable = methodBody.getFirstByText(variableStatement)
            methodBody.addAfter(statements, variable)
            val expected = listOf(variableStatement) + STATEMENT_LINES + listOf(returnStatement)
            methodBody.statements.map { it.text } shouldBe expected
        }

        @Test
        fun `add statements before the given element`() {
            val statements = elementFactory.createStatementsFromText(STATEMENTS_TEXT, null)
            val variable = methodBody.getFirstByText(variableStatement)
            methodBody.addBefore(statements, variable)
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
    """.trim()

private val STATEMENT_LINES = STATEMENTS_TEXT.lines()
    .map { it.trim() }
