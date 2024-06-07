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

import com.intellij.psi.PsiMethod
import io.spine.tools.psi.addFirst
import io.spine.tools.psi.java.Environment.elementFactory

/**
 * Annotates this method using the given annotation code.
 */
public fun PsiMethod.annotate(annotationCode: String) {
    val annotation = elementFactory.createAnnotationFromText(annotationCode, containingClass)
    modifierList.addFirst(annotation)
}

/**
 * Creates Javadoc comment and adds it as the first element of this method.
 */
public fun PsiMethod.addJavadoc(text: String) {
    val doc = elementFactory.createDocCommentFromText(text)
    addFirst(doc)
}

/**
 * Annotates this method with `@Override`.
 */
public fun PsiMethod.annotateOverride() {
    annotate(Override::class.java)
}

/**
 * Tells if this method is annotated with the given annotation.
 */
public fun PsiMethod.isAnnotated(cls: Class<out Annotation>): Boolean {
    return modifierList.findAnnotation(cls.name) != null
            // If PSI is not supplied with JDK, annotations from `java.lang` are not resolved.
            // Double check using the simple class name.
            || modifierList.findAnnotation(cls.simpleName) != null
}

/**
 * Annotates this method with the given annotation if it's not yet added.
 */
public fun PsiMethod.annotate(cls: Class<out Annotation>) {
    if (!isAnnotated(cls)) {
        val annotation = elementFactory.createAnnotationFromText("@${cls.reference}", this)
        modifierList.addBefore(annotation, modifierList.firstChild)
    }
}

// We do have similar extensions in the `tool-base` module,
// but we don't want to add the dependency on it just because of these small bits.

private val <T : Annotation> Class<T>.isJavaLang: Boolean
    get() = name.contains("java.lang")

private val <T : Annotation> Class<T>.reference: String
    get() = if (isJavaLang) simpleName else canonicalName
