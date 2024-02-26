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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiField
import com.intellij.psi.PsiType
import com.intellij.psi.impl.JavaPsiFacadeImpl
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.search.GlobalSearchScope
import io.spine.code.java.ClassName

@Suppress("NonExtendableApiUsage")
internal class MockJavaPsiFacade(project: Project) : JavaPsiFacadeImpl(project) {

    override fun findClass(qualifiedName: String, scope: GlobalSearchScope): PsiClass? {
        val foundClass = super.findClass(qualifiedName, scope)
        if (foundClass != null) {
            return foundClass
        }
        val clsName = ClassName.of(qualifiedName)

        val simpleName = clsName.toSimple().value
        if(qualifiedName == simpleName) {
            // Meaning, no package set!
            return null
        }

        val startsWithCapital = simpleName.get(0).isUpperCase()
        if (!startsWithCapital) {
            return null
        }

        val synthetic = try {
//            Thread.currentThread().contextClassLoader.loadClass(qualifiedName)
            MockPsiClassFactory.get(clsName)
        } catch (e: ClassNotFoundException ) {
            return null
        }
        return synthetic
    }
}

private object MockPsiClassFactory {

    private const val LIMIT = 300L

    fun get(qualifiedName: ClassName): PsiClass {
        return cache.get(qualifiedName)
    }

    private val cache: LoadingCache<ClassName, PsiClass> =
        CacheBuilder.newBuilder()
            .maximumSize(LIMIT)
            .build(Loader)

    private val elementFactory: PsiElementFactory by lazy {
        JavaPsiFacade.getElementFactory(Environment.project)
    }

    private val parser: Parser by lazy {
        Parser(Environment.project)
    }

    private object Loader : CacheLoader<ClassName, PsiClass>() {
        override fun load(className: ClassName): PsiClass {
            val simpleName = className.toSimple().value
            val mockFile = """
            package ${className.packageName().value};
            public class $simpleName {
            }                
            """.trimIndent()
            val mockPsiFile = parser.parse(mockFile)
            val cls = mockPsiFile.topLevelClass
            val endOfClass = cls.children.last()
            val wrapper = object : PsiClassImpl(cls.node) {
                private val serialVersionUID: Long = 7142432410196103694L

                override fun findInnerClassByName(name: String?, checkBases: Boolean): PsiClass? {
                    val original = cls.findInnerClassByName(name, checkBases)
                    if(original != null) {
                        return original
                    }
                    if(!name!!.first().isUpperCase()) {
                        return null
                    }

                    val inner = elementFactory.createClassFromText(
                        """
                        public static class $name { }
                    """.trimIndent(), cls
                    ).allInnerClasses.first()
                    val addedElement = cls.addBefore(inner, endOfClass)
                    return addedElement as PsiClass
                }

                override fun findFieldByName(name: String?, checkBases: Boolean): PsiField? {
                    val original = cls.findFieldByName(name, checkBases)
                    if (original != null) {
                        return original
                    }

                    if (!name!!.first().isLowerCase()) {
                        return null
                    }

                    val field = elementFactory.createField(name, PsiType.LONG)
                    val addedElement = cls.addBefore(field, endOfClass)
                    return addedElement as PsiField
                }
            }
            return wrapper
        }
    }
}
