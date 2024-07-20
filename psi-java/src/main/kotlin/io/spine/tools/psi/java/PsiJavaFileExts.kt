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
import com.intellij.psi.PsiJavaFile

/**
 * Obtains the top class declared in this Java file.
 */
public val PsiJavaFile.topLevelClass: PsiClass
    get() {
        check(classes.isNotEmpty()) {
            "The Java file `$this` has no classes."
        }
        return classes[0]
    }

/**
 * Locates a class by its simple name in the given Java file.
 *
 * If a class is nested, the simple name must include the names of all the enclosing classes
 * from outermost to innermost.
 */
public fun PsiJavaFile.locate(vararg simpleName: String): PsiClass? =
    locate(simpleName.asIterable())

/**
 * Locates a class by its simple name in the given Java file.
 *
 * If a class is nested, the simple name must include the names of all the enclosing classes
 * from outermost to innermost.
 */
@Suppress("ReturnCount")
public fun PsiJavaFile.locate(simpleName: Iterable<String>): PsiClass? {
    val names = simpleName.toMutableList()
    require(names.isNotEmpty()) {
        "The list of simple class names must not be empty."
    }
    // There's only one top-level class in a Java file.
    val topLevel = classes.first()
    if (topLevel.name != names[0]) {
        // The top-level class in the file does not match the first simple name.
        // No need to look further.
        return null
    }
    if (names.size == 1) {
        return topLevel
    }
    // Proceed to nested classes.
    names.removeAt(0)

    var currentClass = topLevel

    while(currentClass != null) {
        currentClass.innerClasses.firstOrNull { it.name == names[0] }?.let {
            if (names.size == 1) {
                return it
            }
            names.removeAt(0)
            currentClass = it
        } ?: return null // None of the nested classes matches the next simple name.
    }
    return null
}
