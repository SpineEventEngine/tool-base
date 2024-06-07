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
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiMethod
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.tools.java.reference
import io.spine.tools.psi.java.Environment.elementFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("`PsiClass` extensions should")
internal class PsiClassExtsSpec: PsiTest() {

    private lateinit var cls: PsiClass

    @BeforeEach
    fun createClass() {
        cls = elementFactory.createClass("Stub")
        execute { cls.removePublic() }
    }

    @Test
    fun `obtain line number`() {
        val file = parse("LineNumberTest.java")
        cls = file.topLevelClass
        cls.lineNumber shouldBeGreaterThan 0
    }

    @Test
    fun `find a method by its name`() {
        val file = parse("FileOnDisk.java")
        cls = file.topLevelClass
        val method: PsiMethod = assertDoesNotThrow {
            cls.method("main")
        }
        method.isStatic shouldBe true
        method.isPublic shouldBe true
    }

    @Test
    fun `add an element first`() {
        val methodName = "doFirst"
        val method = elementFactory.createStubMethod(methodName)
        execute {
            cls.addFirst(method)
        }
        (cls.firstChild is PsiMethod) shouldBe true
        // Cannot use equality with `PsiMethod` because `equals()` is not overridden.
        (cls.firstChild as PsiMethod).name shouldBe method.name
    }

    @Test
    fun `add an element before the closing brace`() {
        val methodName = "doLast"
        val method = elementFactory.createStubMethod(methodName)
        execute {
            cls.addLast(method)
        }
        val closingBraceIndex = cls.children.indexOf(cls.rBrace)
        val preLastChild = cls.children[closingBraceIndex - 1]

        (preLastChild is PsiMethod) shouldBe true
        (preLastChild as PsiMethod).name shouldBe method.name
    }

    @Nested inner class
    `Add super type` {

        private val runnable: PsiJavaCodeReferenceElement by lazy {
            elementFactory.createClassReference(className = Runnable::class.java.reference)
        }

        private val function: PsiJavaCodeReferenceElement by lazy {
            elementFactory.createClassReference(
                className = java.util.function.Function::class.java.reference
            )
        }

        @Test
        fun `if the class does not have a super type`() {
            cls.run {
                hasSuperclass() shouldBe false
                execute {
                    addSuperclass(runnable)
                }
                explicitSuperclass shouldNotBe null
                explicitSuperclass!!.qualifiedName shouldBe Runnable::class.java.reference
            }
        }

        @Test
        fun `preventing adding if superclass already exists`() {
            execute {
                cls.addSuperclass(function)
                assertThrows<IllegalStateException> {
                    cls.addSuperclass(runnable)
                }
            }
        }
    }
}
