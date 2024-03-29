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

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiJavaFile
import io.spine.tools.psi.readResource
import java.io.File
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

/**
 * Abstract base for test suites which need PSI [Environment].
 */
@Suppress(
    "UtilityClassWithPublicConstructor" // Adds `@BeforeAll` and `@AfterAll` for derived classes.
)
abstract class PsiTest {

    companion object {

        lateinit var project: Project

        val parser: Parser by lazy {
            Parser(project)
        }

        @JvmStatic
        @BeforeAll
        fun setupIdea() {
            Environment.setUp()
            project = Environment.project
        }

        @JvmStatic
        @AfterAll
        fun dispose() {
            Environment.close()
        }

        fun parse(fileName: String): PsiJavaFile {
            val code = readResource(fileName)
            val file = File(fileName)
            val psiFile = parser.parse(code, file)
            return psiFile
        }
    }
}
