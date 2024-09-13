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
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiMethod
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.testing.TestValues
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

    private val packageName = "org.example.psi.extensions"

    private val fileFactory by lazy {
        PsiFileFactory.getInstance(project)
    }

    private lateinit var cls: PsiClass

    @BeforeEach
    fun createClass() {
        val suffix = TestValues.random(1000)
        val className = "Stub$suffix"
        val psiFile = fileFactory.createJavaFile(packageName, className)
        cls = psiFile.topLevelClass
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
    `set superclass` {

        private val arrayList: PsiJavaCodeReferenceElement by lazy {
            elementFactory.createClassReference(
                className = java.util.ArrayList::class.java.reference
            )
        }

        private val hashMap: PsiJavaCodeReferenceElement by lazy {
            elementFactory.createClassReference(
                className = java.util.HashMap::class.java.reference
            )
        }

        @Test
        fun `if the class does not have an explicit superclass`() {
            cls.run {
                hasSuperclass() shouldBe false
                execute {
                    setSuperclass(arrayList)
                }
                explicitSuperclass shouldNotBe null
                explicitSuperclass!!.qualifiedName shouldBe
                        java.util.ArrayList::class.java.reference
            }
        }

        @Test
        fun `preventing adding if a superclass is already defined`() {
            execute {
                cls.setSuperclass(hashMap)
                assertThrows<IllegalStateException> {
                    cls.setSuperclass(arrayList)
                }
            }
        }
    }

    @Nested inner class
    `make a class or interface implement an interface` {

        private val runnable: PsiJavaCodeReferenceElement by lazy {
            elementFactory.createClassReference(className = Runnable::class.java.reference)
        }

        private val function: PsiJavaCodeReferenceElement by lazy {
            elementFactory.createClassReference(
                className = java.util.function.Function::class.java.reference
            )
        }

        @Test
        fun `if no interfaces are implemented`() {
            cls.implementsInterfaces() shouldBe false

            execute {
                cls.implement(runnable)
            }

            cls.implementsList.run {
                this shouldNotBe null
                this!!
                referenceElements.size shouldBe 1
                referenceElements[0].qualifiedName shouldBe runnable.qualifiedName
            }
        }

        @Test
        fun `if an interface already implemented`() {
            cls.implementsInterfaces() shouldBe false
            execute {
                cls.implement(runnable)
            }

            assertDoesNotThrow {
                execute {
                    cls.implement(runnable)
                }
            }

            cls.implementsList.run {
                this!!
                referenceElements.size shouldBe 1
                referenceElements[0].qualifiedName shouldBe runnable.qualifiedName
            }
        }

        @Test
        fun `if another interface is already implemented`() {
            cls.implementsInterfaces() shouldBe false
            execute {
                cls.implement(runnable)
            }

            assertDoesNotThrow {
                execute {
                    cls.implement(function)
                }
            }

            cls.implementsList.run {
                this!!
                referenceElements.size shouldBe 2
                referenceElements[0].qualifiedName shouldBe runnable.qualifiedName
                referenceElements[1].qualifiedName shouldBe function.qualifiedName
            }
        }
    }

    @Nested inner class
    `tell if a class implements an interface` {

        private val shortName = "BaseInterface"
        private val shortReference = elementFactory.createInterfaceReference(shortName)

        /**
         * The reference in the same package.
         */
        private val qualifiedReference =
            elementFactory.createInterfaceReference("$packageName.$shortName")

        @Test
        fun `in the same package via short reference`() {
            execute {
                cls.implement(shortReference)
            }

            cls.implements(shortReference) shouldBe true
            cls.implements(qualifiedReference) shouldBe true
        }

        @Test
        fun `in the same package via qualified reference`() {
            execute {
                cls.implement(qualifiedReference)
            }

            cls.implements(qualifiedReference) shouldBe true
            cls.implements(shortReference) shouldBe true
        }

        @Test
        fun `in another package`() {
            val inAnotherPackage =
                elementFactory.createInterfaceReference("another.pckg.$shortName")
            execute {
                cls.implement(inAnotherPackage)
            }

            cls.implements(inAnotherPackage) shouldBe true
        }
    }
}
