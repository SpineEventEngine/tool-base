/*
 * Copyright 2024, TeamDev. All rights reserved.
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

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@DisplayName("'PsiMethod' extensions should")
class PsiMethodExtsSpec: PsiTest() {

    private lateinit var cls: PsiClass
    private lateinit var file: PsiJavaFile

    @BeforeEach
    fun parseFile() {
        file = parse("Methods.java")
        cls = file.topLevelClass
    }

    @Test
    fun `annotate a method`() {
        val mainMethod = cls.method("main")
        val annotationCode = "@SuppressWarnings(\"ALL\")"
        execute {
            mainMethod.annotate(annotationCode)
        }

        file.text shouldContain annotationCode
    }

    @Nested inner class
    `Mark a method with '@Override'`() {

        @Test
        fun `if the method is not annotated`() {
            val method = cls.method("test")
            method.run {
                isAnnotated(Override::class.java) shouldBe false
                execute {
                    annotateOverride()
                }
                isAnnotated(Override::class.java) shouldBe true
            }
        }

        @Test
        fun `accepting if the method is already annotated`() {
            val method = cls.method("run")
            method.run {
                isAnnotated(Override::class.java) shouldBe true
                assertDoesNotThrow {
                    execute {
                        annotateOverride()
                    }
                }
                isAnnotated(Override::class.java) shouldBe true
            }
        }
    }
}
