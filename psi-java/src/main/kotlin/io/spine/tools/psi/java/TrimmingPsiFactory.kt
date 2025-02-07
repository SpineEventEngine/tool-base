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

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiJavaModule
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiRecordHeader
import com.intellij.psi.PsiResourceVariable
import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.PsiTypeParameter

/**
 * Wraps the provided [PsiElementFactory] to allow the passed text representation
 * of Java elements contain leading whitespaces.
 *
 * This decorator performs preliminary trimming of the passed Java text, so that
 * to bypass strings with leading whitespaces. PSI throws upon them.
 * We are not afraid to lose in terms of formatting because of the following:
 *
 * 1. Leading whitespaces are never used intentionally for particular elements.
 *    Formatting usually goes somewhere inside the element.
 * 2. The resulting code is usually auto-formatted.
 *
 * When calling [PsiElementFactory] from Kotlin, we can use multiline string
 * expressions to pass Java text. Such strings are never indented automatically as,
 * for example, text blocks in Java 15. It is `trimIndent()`, `trimMargin()`,
 * or just `trim()` that should be explicitly invoked. These methods behave differently;
 * it is quite easy to misuse them, leaving the resulting string with an unexpected indentation.
 *
 * For example, the code snippet below:
 * ```
 * val statements = """
 *     System.out.println("Hello world!");
 *     System.out.println("My name is Jack.");
 * """.trimIndent()
 *
 * val main = """
 *     public static void print() {
 *         $statements
 *     }
 * """.trimIndent()
 * ```
 *
 * Leads to the following content of `main` variable:
 * ```
 *     public static void print() {
 *         System.out.println("Hello world!");
 * System.out.println("My name is Jack.");
 *     }
 * ```
 *
 * Instead of the expected:
 * ```
 * public static void print() {
 *     System.out.println("Hello world!");
 *     System.out.println("My name is Jack.");
 * }
 * ```
 *
 * The following methods actually bypass leading whitespaces,
 * so we do not override them. Sometimes, trims on its own
 * or use wrapping element.
 *
 * 1. [PsiElementFactory.createDocTagFromText]. Trims on its own.
 * 2. [PsiElementFactory.createDocCommentFromText]. Trims on its own.
 * 3. [PsiElementFactory.createClassFromText]. Actually, the created class is nested in `Dummy`.
 * 4. [PsiElementFactory.createModuleStatementFromText]. Trims on its own.
 * 5. [PsiElementFactory.createModuleReferenceFromText]. Trims on its own.
 */
@Suppress("TooManyFunctions")
internal class TrimmingPsiFactory(
    private val delegate: PsiElementFactory
) : PsiElementFactory by delegate {

    override fun createFieldFromText(p0: String, p1: PsiElement?): PsiField =
        delegate.createFieldFromText(p0.trimStart(), p1)

    override fun createMethodFromText(p0: String, p1: PsiElement?, p2: LanguageLevel?): PsiMethod =
        delegate.createMethodFromText(p0.trimStart(), p1, p2)

    override fun createMethodFromText(p0: String, p1: PsiElement?): PsiMethod =
        delegate.createMethodFromText(p0.trimStart(), p1)

    override fun createParameterFromText(p0: String, p1: PsiElement?): PsiParameter =
        delegate.createParameterFromText(p0.trimStart(), p1)

    override fun createRecordHeaderFromText(p0: String, p1: PsiElement?): PsiRecordHeader =
        delegate.createRecordHeaderFromText(p0.trimStart(), p1)

    override fun createResourceFromText(p0: String, p1: PsiElement?): PsiResourceVariable =
        delegate.createResourceFromText(p0.trimStart(), p1)

    override fun createTypeFromText(p0: String, p1: PsiElement?): PsiType =
        delegate.createTypeFromText(p0.trimStart(), p1)

    override fun createTypeElementFromText(p0: String, p1: PsiElement?): PsiTypeElement =
        delegate.createTypeElementFromText(p0.trimStart(), p1)

    override fun createReferenceFromText(p0: String, p1: PsiElement?): PsiJavaCodeReferenceElement =
        delegate.createReferenceFromText(p0.trimStart(), p1)

    override fun createCodeBlockFromText(p0: String, p1: PsiElement?): PsiCodeBlock =
        delegate.createCodeBlockFromText(p0.trimStart(), p1)

    override fun createStatementFromText(p0: String, p1: PsiElement?): PsiStatement =
        delegate.createStatementFromText(p0.trimStart(), p1)

    override fun createExpressionFromText(p0: String, p1: PsiElement?): PsiExpression =
        delegate.createExpressionFromText(p0.trimStart(), p1)

    // Use `trim()`.
    override fun createCommentFromText(p0: String, p1: PsiElement?): PsiComment =
        delegate.createCommentFromText(p0.trim(), p1)

    override fun createTypeParameterFromText(p0: String, p1: PsiElement?): PsiTypeParameter =
        delegate.createTypeParameterFromText(p0.trimStart(), p1)

    override fun createAnnotationFromText(p0: String, p1: PsiElement?): PsiAnnotation =
        delegate.createAnnotationFromText(p0.trimStart(), p1)

    override fun createEnumConstantFromText(p0: String, p1: PsiElement?): PsiEnumConstant =
        delegate.createEnumConstantFromText(p0.trimStart(), p1)

    // Use `trim()`.
    override fun createPrimitiveTypeFromText(p0: String): PsiType =
        delegate.createPrimitiveTypeFromText(p0.trim())

    override fun createModuleFromText(p0: String, p1: PsiElement?): PsiJavaModule =
        delegate.createModuleFromText(p0.trimStart(), p1)
}
