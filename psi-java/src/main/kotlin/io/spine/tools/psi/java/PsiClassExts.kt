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

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier.STATIC
import com.intellij.psi.PsiModifierList
import io.spine.tools.psi.document

/**
 * Obtains the line number of the class declaration in the containing file.
 *
 * @return the line where the name identifier of the class is placed.
 */
public val PsiClass.lineNumber: Int
    get() = document.getLineNumber(textOffset)

/**
 * Obtains a method declared directly in this class.
 *
 * @throws IllegalStateException
 *          if the class does not have a method with the given name.
 */
public fun PsiClass.method(name: String): PsiMethod {
    val found = findMethodsByName(name, false)
    check(found.isNotEmpty()) {
        "The class `$qualifiedName` does not declare a method named `$name`."
    }
    return found.first()
}

/**
 * Obtains the list of modifiers for this class.
 *
 * @throws IllegalStateException
 *          if the class does not have a list of modifiers, which could be the case,
 *          for example, for an anonymous class.
 */
public val PsiClass.modifiers: PsiModifierList
    get() {
        check(modifierList != null) {
            "The class $this does not have modifiers"
        }
        return modifierList!!
    }

/**
 * Tells if this class has the [`static`][STATIC] modifier.
 */
public val PsiClass.isStatic: Boolean
    get() = modifiers.hasModifierProperty(STATIC)

/**
 * Adds `static` modifier to this class, if it did not have the modifier before.
 */
public fun PsiClass.makeStatic(): PsiClass {
    modifiers.setIfAbsent(STATIC)
    return this
}

/**
 * Adds given [element] before the closing brace of this class.
 */
public fun PsiClass.addLast(element: PsiElement): PsiClass {
    addBefore(element, rBrace)
    return this
}
