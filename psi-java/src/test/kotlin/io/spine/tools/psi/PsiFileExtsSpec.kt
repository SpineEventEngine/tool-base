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

package io.spine.tools.psi

import com.intellij.psi.PsiCodeBlock
import io.kotest.matchers.shouldBe
import io.spine.testing.TestValues.randomString
import io.spine.tools.psi.java.Environment.elementFactory
import io.spine.tools.psi.java.FileSystem
import io.spine.tools.psi.java.canonicalCode
import java.nio.file.Path
import kotlin.io.path.writeText
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * This test suite is for extensions of [PsiFile][com.intellij.psi.PsiFile] which
 * are declared in the `PsiFileExts.kt` of the `psi` module.
 *
 * We have these tests here, under `psi-java` module, because this module
 * has [io.spine.tools.psi.java.Environment] with all the necessary PSI services configured.
 * Some of these services are language-independent, some of them are specific to Java.
 *
 * [PsiFile][com.intellij.psi.PsiFile] is language independent interface.
 * Once we have language neutral environment configuration code under the `psi` module,
 * these tests should be moved there too.
 */
@DisplayName("`PsiFile` extensions should")
internal class PsiFileExtsSpec {

    @Test
    fun `load file content`(@TempDir tempDir: Path) {
        val file = tempDir.resolve("Stub.java")
        val content = """/* ${randomString()} */"""
        file.writeText(content)

        val psiFile = FileSystem.load(file)
        psiFile.content() shouldBe content
    }

    @Nested
    @DisplayName("provide canonical code")
    @Suppress(
        "NewClassNamingConvention",
        "InconsistentCommentForJavaParameter",
        "UnusedAssignment",
        "PackageVisibleField",
        "FieldNamingConvention",
        "QuestionableName"
    )
    inner class CanonicalCode {

        @Test
        fun `collapse spaces and drop comments`() {
            @Language("Java")
            val method = elementFactory.createMethodFromText(
                """
                void m() {
                    // line comment
                    int x   =  list . get( 0 ,  1 ) /*block*/ ;
                    x ++ ; /* after */
                    if ( ( x > 0 ) ) { x = x + 1 ; }
                }
                """.trimIndent(), /*context=*/null
            )
            val body = method.body as PsiCodeBlock

            @Language("Java")
            val expected = "{ int x = list.get(0, 1); x++; if ((x > 0)) { x = x + 1; } }"
            // Body text without leading/trailing braces must be normalized.
            body.canonicalCode() shouldBe expected

        }

        @Test
        fun `avoid spaces before punctuation and after openers`() {
            @Language("Java")
            val statement = elementFactory.createStatementFromText(
                """
                foo( a , b ).bar( ( 1 + 2 ) , 3 ); // tail
                """.trimIndent(), /*context=*/null
            )

            @Language("Java")
            val expected = "foo(a, b).bar((1 + 2), 3);"
            statement.canonicalCode() shouldBe expected
        }

        @Test
        @Suppress("MaxLineLength")
        fun `normalize spaces in generic parameters`() {
            @Language("Java")
            val statement = elementFactory.createStatementFromText(
                """
                java.util.Map < String , java.util.List < Integer > > m = new java.util.HashMap < > ();
                """.trimIndent(), /*context=*/null
            )

            @Language("Java")
            val expected = "java.util.Map<String, java.util.List<Integer>> m = new java.util.HashMap<> ();"
            statement.canonicalCode() shouldBe expected
        }

        @Test
        fun `ensure single spaces around arrow`() {
            @Language("Java")
            val statement = elementFactory.createStatementFromText(
                """
                java.util.function.Function< Integer , Integer > f = x->x+1;
                """.trimIndent(), /*context=*/null
            )

            @Language("Java")
            val expected = "java.util.function.Function<Integer, Integer> f = x -> x + 1;"
            statement.canonicalCode() shouldBe expected
        }
    }
}
