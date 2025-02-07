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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiJavaModule
import com.intellij.psi.PsiJavaModuleReferenceElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiRecordHeader
import com.intellij.psi.PsiResourceVariable
import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.PsiTypeParameter
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.javadoc.PsiDocTag

/**
 * Wraps an existing [PsiElementFactory] and intercepts calls to all
 * `create...FromText()` methods.
 *
 * The primary concern is ensuring that the PSI factory does not reject the input
 * due to unwanted whitespace. The decorator sanitizes the input strings by trimming
 * unwanted leading (and in some cases, trailing) whitespaces before passing them
 * to the underlying factory.
 *
 * This is necessary because we often use multiline Kotlin strings to define text,
 * and when being interpolated, the default [trimIndent] usually does nothing to
 * the string literal, leaving all whitespaces as is. This extra whitespace can
 * cause the underlying PSI creation methods to fail.
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
 * Leads to the following content of the `main` variable:
 * ```
 *     public static void print() {
 *         System.out.println("Hello world!");
 * System.out.println("My name is Jack.");
 *     }
 * ```
 *
 * Instead of the expected (notice the lack of leading whitespaces):
 * ```
 * public static void print() {
 *     System.out.println("Hello world!");
 *     System.out.println("My name is Jack.");
 * }
 * ```
 *
 * We are not afraid to lose in terms of formatting because of the following:
 *
 * 1. Formatting usually goes somewhere inside the element definition rather before.
 * 2. At the last stage, the generated code is usually auto-formatted.
 */
@Suppress("TooManyFunctions")
internal class PsiElementFactoryDecorator(
    private val delegate: PsiElementFactory
) : PsiElementFactory by delegate {

    override fun createDocTagFromText(p0: String): PsiDocTag =
        delegate.createDocTagFromText(p0.trimStart())

    override fun createDocCommentFromText(p0: String): PsiDocComment =
        delegate.createDocCommentFromText(p0.trimStart())

    override fun createDocCommentFromText(p0: String, p1: PsiElement?): PsiDocComment =
        delegate.createDocCommentFromText(p0.trimStart(), p1)

    override fun createClassFromText(p0: String, p1: PsiElement?): PsiClass =
        delegate.createClassFromText(p0.trimStart(), p1)

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

    /**
     * Creates a [PsiComment] element from the provided text after sanitizing it.
     *
     * This method fully trims the input text because the Java PSI factory prohibits
     * both leading and trailing whitespaces in comment elements.
     */
    override fun createCommentFromText(p0: String, p1: PsiElement?): PsiComment =
        delegate.createCommentFromText(p0.trim(), p1)

    override fun createTypeParameterFromText(p0: String, p1: PsiElement?): PsiTypeParameter =
        delegate.createTypeParameterFromText(p0.trimStart(), p1)

    override fun createAnnotationFromText(p0: String, p1: PsiElement?): PsiAnnotation =
        delegate.createAnnotationFromText(p0.trimStart(), p1)

    override fun createEnumConstantFromText(p0: String, p1: PsiElement?): PsiEnumConstant =
        delegate.createEnumConstantFromText(p0.trimStart(), p1)

    /**
     * Creates a [PsiType] element for primitives from the provided text after sanitizing it.
     *
     * This method fully trims the input text because the Java PSI factory prohibits
     * both leading and trailing whitespaces in primitive type elements.
     */
    override fun createPrimitiveTypeFromText(p0: String): PsiType =
        delegate.createPrimitiveTypeFromText(p0.trim())

    override fun createModuleFromText(p0: String, p1: PsiElement?): PsiJavaModule =
        delegate.createModuleFromText(p0.trimStart(), p1)

    override fun createModuleStatementFromText(p0: String, p1: PsiElement?): PsiStatement =
        delegate.createModuleStatementFromText(p0.trimStart(), p1)

    override fun createModuleReferenceFromText(
        p0: String,
        p1: PsiElement?
    ): PsiJavaModuleReferenceElement = delegate.createModuleReferenceFromText(p0.trimStart(), p1)
}
