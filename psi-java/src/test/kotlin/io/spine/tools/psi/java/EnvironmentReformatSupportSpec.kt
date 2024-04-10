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

import com.intellij.psi.codeStyle.arrangement.MemberOrderService
import com.intellij.psi.impl.source.codeStyle.IndentHelper
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.spine.tools.psi.codeStyleManager
import io.spine.tools.psi.java.Environment.application
import io.spine.tools.psi.service
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@DisplayName("`Environment` should configure PSI for")
internal class EnvironmentReformatSupportSpec : PsiTest() {

    private val project = Environment.project

    @Test
    fun `obtaining 'CodeStyleManager'`() {
        assertDoesNotThrow {
            project.codeStyleManager
        }
    }

    @Test
    fun `reformat of 'PsiJavaFile'`() {
        val psiFile = parse("FieldPath.java")

        execute {
            project.codeStyleManager.reformat(psiFile)
        }

        val text = psiFile.text

        // The static field, which was not indented, got indentation of 4 spaces.
        text shouldContain "    private static final long serialVersionUID = 0L;"
    }

    @Test
    fun `obtaining 'IndentHelper' instance`() {
        val indentHelper = assertDoesNotThrow {
            IndentHelper.getInstance()
        }
        indentHelper shouldNotBe null
    }

    @Test
    fun `obtaining 'MemberOrderService' instance`() {
        val orderService = application.service<MemberOrderService>()
        orderService shouldNotBe null
    }
}
