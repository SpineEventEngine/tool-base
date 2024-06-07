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
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.javadoc.PsiDocComment
import io.spine.string.containsLineSeparators
import org.jetbrains.annotations.TestOnly

/**
 * Creates a private no-op constructor for the given class.
 *
 * The created constructor accepts no parameters.
 * This function could be useful for creating constructors of utility classes, or
 * for classes that should not be instantiated from outside the code of
 * the generated class.
 *
 * @param javadocLine
 *         optional single-line text for Javadoc comment to be generated.
 *         If `null`, no Javadoc comment will be created.
 * @return the generated constructor.
 * @see PsiElementFactory.createMethodFromText
 */
public fun PsiElementFactory.createPrivateConstructor(
    cls: PsiClass,
    javadocLine: String? = null,
): PsiMethod {
    val ctor = createMethodFromText("""
        private ${cls.name}() {
        }            
        """.trimIndent(), cls
    )
    if (javadocLine != null) {
        val javadoc = createJavadoc(javadocLine)
        ctor.addBefore(javadoc, ctor.firstChild)
    }
    return ctor
}

/**
 * Creates a single-line Javadoc comment using the given text.
 *
 * @param line
 *         a non-empty one-line text of the Javadoc comment.
 * @throws IllegalArgumentException
 *         if the given line is empty or contains line separators.
 * @return the generated Javadoc comment.
 * @see PsiElementFactory.createDocCommentFromText
 */
public fun PsiElementFactory.createJavadoc(line: String): PsiDocComment {
    require(line.isNotEmpty()) {
        "Unable to create a Javadoc comment with an empty text."
    }
    require(!line.containsLineSeparators()) {
        "Please use `createDocCommentFromText()` for creating multi-line Javadoc comments."
    }
    val javadoc = createDocCommentFromText("""
        /** $line */    
        """.trimIndent()
    )
    return javadoc
}

/**
 * Creates a no-op method which returns `void`.
 *
 * Use this extension for creating stub instances of [PsiMethod] for code generation tests.
 */
@TestOnly
public fun PsiElementFactory.createStubMethod(name: String): PsiMethod {
    val method = createMethodFromText("""
        void $name() {}
        """.trimIndent(), null
    )
    return method
}

/**
 * Creates a class type which represents the Java class specified by the generic parameter [T].
 */
public inline fun <reified T: Any> PsiElementFactory.createClassType(): PsiClassType {
    val clsType = createTypeByFQClassName(T::class.java.canonicalName)
    return clsType
}

/**
 * Creates a reference to the class with the given name.
 *
 * @param context
 *         the PSI element used as the context for resolving the reference.
 * @param className
 *         the name of the class to reference. It could be fully qualified or
 *         a simple name.
 * @param genericParams
 *         optional generic parameters if the class to reference is generic.
 * @return new class reference.
 */
public fun PsiElementFactory.createClassReference(
    context: PsiElement? = null,
    className: String,
    vararg genericParams: String
): PsiJavaCodeReferenceElement {
    return if (genericParams.isEmpty()) {
        createReferenceFromText(className, context)
    } else {
        val paramsText = genericParams.joinToString(", ")
        val superClassRef = "$className<$paramsText>"
        createReferenceFromText(superClassRef, context)
    }
}
