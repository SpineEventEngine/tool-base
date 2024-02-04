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
import java.io.File
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.spine.tools.psi.readResource
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`Parser` should")
class ParserSpec : ParsingTest() {

    companion object {

        lateinit var parser: Parser
        lateinit var code: String

        @JvmStatic
        @BeforeAll
        fun setupIdea() {
            Environment.setup()
            parser = Parser(Environment.project)
            code = readResource("FileOnDisk.java")
        }
    }

    @Nested inner class
    `parse loaded Java code` {

        @Test
        fun `providing synthetic file name`() {
            val psiJavaFile = parser.parse(code)
            psiJavaFile.name shouldStartWith "__to_parse_"
        }

        @Test
        fun `using passed file reference`() {
            val file = File("path/to/file.java")
            val psiJavaFile: PsiJavaFile = parser.parse(code, file)

            psiJavaFile.name shouldContain file.toString()
        }
    }
}
