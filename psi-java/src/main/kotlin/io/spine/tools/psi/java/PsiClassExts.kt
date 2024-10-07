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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierList
import io.spine.tools.psi.document
import io.spine.tools.psi.java.Environment.elementFactory

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
 * @throws IllegalStateException If the class does not have a method with the given name.
 */
public fun PsiClass.method(name: String): PsiMethod {
    val found = findMethodsByName(name, false)
    check(found.isNotEmpty()) {
        "The class `$qualifiedName` does not declare a method named `$name`."
    }
    return found.first()
}

/**
 * Obtains the package to which this class belongs.
 *
 * @returns The package name or an empty string if the class does not belong to a package.
 */
public val PsiClass.packageName: String
    get() {
        var current = this
        while (current.containingClass != null) {
            current = current.containingClass!!
        }
        val qualifiedName = current.qualifiedName
        val result = qualifiedName?.substringBeforeLast('.')
        return result ?: ""
    }

/**
 * Obtains the list of modifiers for this class.
 *
 * @throws IllegalStateException If the class does not have a list of modifiers,
 *   which could be the case, for example, for an anonymous class.
 */
public val PsiClass.modifiers: PsiModifierList
    get() {
        check(modifierList != null) {
            "The class $this does not have modifiers"
        }
        return modifierList!!
    }

/**
 * Adds given [element] before the [firstChild][PsiClass.getFirstChild] of this class.
 */
public fun PsiClass.addFirst(element: PsiElement): PsiElement {
    return addBefore(element, firstChild)
}

/**
 * Adds given [element] before the closing brace of this class.
 */
public fun PsiClass.addLast(element: PsiElement): PsiClass {
    addBefore(element, rBrace)
    return this
}

/**
 * Tells if this class has any explicitly declared superclass.
 *
 * In Java all the classes implicitly extend [java.lang.Object].
 * The function checks only explicitly defined superclass.
 *
 * @return `true` if the class is anonymous or has no `extends` clause, `false` otherwise.
 * @see explicitSuperclass
 */
public fun PsiClass.hasSuperclass(): Boolean {
    return extendsList?.children?.isNotEmpty() ?: false
}

/**
 * Obtains a reference to a superclass, if any, this class extends.
 *
 * In Java all the classes implicitly extend [java.lang.Object].
 * The value provides only explicitly defined superclass.
 *
 * @see hasSuperclass
 */
public val PsiClass.explicitSuperclass: PsiJavaCodeReferenceElement?
    get() = if (hasSuperclass()) {
        extendsList?.children?.last() as PsiJavaCodeReferenceElement
    } else {
        null
    }

/**
 * Makes this class extend the class specified by the given [superClass].
 *
 * @throws IllegalStateException
 *          if called for a receiver representing an anonymous class, or
 *          when the receiver already extends another class.
 * @see setSuperclass
 * @see implement
 */
@Deprecated(
    message = "Please use `setSuperclass()` instead.",
    replaceWith = ReplaceWith("setSuperclass()")
)
public fun PsiClass.addSuperclass(superClass: PsiJavaCodeReferenceElement) {
    setSuperclass(superClass)
}

/**
 * Makes this class extend the class specified by the given [superClass].
 *
 * @throws IllegalStateException
 *          if called for a receiver representing an anonymous class, or
 *          when the receiver already extends another class.
 * @see implement
 */
public fun PsiClass.setSuperclass(superClass: PsiJavaCodeReferenceElement) {
    check(extendsList != null) {
        "Cannot add a superclass to an anonymous class `$this`."
    }
    check(
        explicitSuperclass == null ||
                explicitSuperclass?.qualifiedName == superClass.qualifiedName
    ) {
        "The class `$qualifiedName` cannot be derived from ${superClass.qualifiedName} " +
        "because it already extends `${explicitSuperclass?.qualifiedName}`."
    }
    extendsList!!.add(superClass)
}

/**
 * Tells if this class implements one or more interfaces.
 */
public fun PsiClass.implementsInterfaces(): Boolean {
    // It's not likely that `implementsList` is `null`, but this way we cover possible future
    // changes in the PSI implementation.
    return implementsList?.referenceElements?.isNotEmpty() ?: false
}

/**
 * Tells if this class implements the given interface.
 */
@JvmName("doesImplement")
public fun PsiClass.implements(superInterface: PsiJavaCodeReferenceElement): Boolean {
    val qualified = superInterface.qualifier != null
    val samePackage = this.packageName == superInterface.qualifier?.text

    return implementsList?.referenceElements?.any {
        (it.qualifiedName == superInterface.qualifiedName) ||
                ((samePackage || qualified.not()) &&
                        (it.referenceName == superInterface.referenceName))
    } ?: false
}

/**
 * Makes a Java class represented by this [PsiClass] implement the given interface.
 *
 * If this class or interface already implements the interface, the function does nothing.
 *
 * @see setSuperclass
 */
public fun PsiClass.implement(superInterface: PsiJavaCodeReferenceElement) {
    val implements = implementsList
    if (implements == null) {
        val newList = elementFactory.createReferenceList(arrayOf(superInterface))
        addBefore(newList, lBrace)
        return
    }
    if (!implements(superInterface)) {
        implements.add(superInterface)
    }
}
