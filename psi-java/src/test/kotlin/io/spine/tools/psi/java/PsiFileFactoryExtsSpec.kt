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

import com.intellij.psi.PsiFileFactory
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.spine.tools.psi.java.Environment.project
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`PsiFileFactory` extensions should")
internal class PsiFileFactoryExtsSpec {

    private val factory = PsiFileFactory.getInstance(project)

    @Nested inner class
    `create Java file` {

        private val simpleName = "MyClass"

        @Test
        fun `requiring a simple class name`() {

            fun assertIllegal(className: String) {
                assertThrows<IllegalArgumentException> {
                    factory.createJavaFile("", className)
                }
            }

            assertIllegal("")
            assertIllegal("org.example.$simpleName")
            assertIllegal("$simpleName.Nested")
            assertIllegal("$simpleName\$Nested")
        }

        @Test
        fun `without a package`() {
            val file = factory.createJavaFile("", simpleName)

            file.text shouldNotContain "package"
        }

        @Test
        fun `with a package`() {
            val expectedPackage = "org.example"
            val file = factory.createJavaFile(expectedPackage, simpleName)

            file.text shouldContain "package $expectedPackage;\n\n"
        }

        @Test
        fun `with modifiers`() {
            val publicCls = factory.createJavaFile("", simpleName, publicClass = true)
            publicCls.text shouldContain "public class $simpleName {"

            val finalCls = factory.createJavaFile("", simpleName, finalClass = true)
            finalCls.text shouldContain "final class $simpleName {"

            val publicFinalCls =
                factory.createJavaFile("", simpleName, publicClass = true, finalClass = true)
            publicFinalCls.text shouldContain "public final class $simpleName {"
        }
    }
}
