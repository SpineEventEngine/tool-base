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
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

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

    @Test
    fun `throw if empty statements is asked for first or last child`() {
        val statements = elementFactory.createStatementsFromText("", null)
        assertThrows<IllegalStateException> {
            statements.firstChild
        }
        assertThrows<IllegalStateException> {
            statements.lastChild
        }
    }

    @Test
    fun `provide Java text`(): Unit = with(elementFactory) {
        createStatementsFromText("", null).text shouldBe ""
        createStatementsFromText(PRINT_STATEMENT, null).text shouldBe PRINT_STATEMENT
        createStatementsFromText(STATEMENTS_TEXT, null).text shouldBe STATEMENTS_TEXT
    }

    /**
     * Makes sure that [PsiStatements] creates its own copy of the passed code block.
     *
     * Every PSI child stores information about its siblings, allowing range operations
     * like `addRange()` accept the first child and last child instances to copy.
     * Otherwise, it would expect a range of indexes. Removing any child from the original
     * code block breaks this traversal, leading to exception in `addRange()` operations.
     */
    @Test
    fun `create a copy of the passed 'CodeBlock'`() {
        val passedBlock = elementFactory.createCodeBlockFromText("{$STATEMENTS_TEXT}", null)
        val statements = PsiStatements(passedBlock)
        execute {
            passedBlock.statements
                .first()
                .delete()
        }
        val anotherBlock = elementFactory.createCodeBlockFromText("{}", null)
        assertDoesNotThrow {
            anotherBlock.add(statements)
        }
    }

    @Test
    fun `append the passed statements`() {
        val statements = elementFactory.createStatementsFromText(STATEMENTS_TEXT, null)
        val printHello = elementFactory.createStatementsFromText(PRINT_STATEMENT, null)
        execute {
            statements.append(printHello)
        }
        statements.firstChild.text shouldBe STATEMENT_LINES.first()
        statements.lastChild.text shouldBe PRINT_STATEMENT
    }

    @Test
    fun `prepend the passed statements`() {
        val statements = elementFactory.createStatementsFromText(STATEMENTS_TEXT, null)
        val printHello = elementFactory.createStatementsFromText(PRINT_STATEMENT, null)
        statements.prepend(printHello)
        statements.firstChild.text shouldBe PRINT_STATEMENT
        statements.lastChild.text shouldBe STATEMENT_LINES.last()
        println(statements.delegate.text)
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
    """.trimIndent()

private const val PRINT_STATEMENT = "System.out.println(\"Hello World!\");"

private val STATEMENT_LINES = STATEMENTS_TEXT.lines()
    .map { it.trim() }
