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

import com.intellij.psi.PsiElement
import io.spine.string.Separator
import io.spine.string.ti

/**
 * Looks for the first child of this [PsiElement], the text representation
 * of which satisfies both [startsWith] and [contains] criteria.
 *
 * This method performs a depth-first search of the PSI hierarchy.
 * So, the second direct child of this [PsiElement] is checked only
 * when the first child and all its descendants are checked.
 *
 * @return the found element, or `null` if this [PsiElement] does not contain such an element.
 */
public fun PsiElement.findFirstByText(
    startsWith: String,
    contains: String = startsWith
): PsiElement? = children.firstNotNullOfOrNull { element ->
    val text = element.text
    when {
        !text.contains(contains) -> null
        text.startsWith(startsWith) -> element
        else -> element.findFirstByText(startsWith, contains)
    }
}

/**
 * Returns the first child of this [PsiElement], the text representation
 * of which satisfies both [startsWith] and [contains] criteria.
 *
 * This method performs a depth-first search of the PSI hierarchy.
 * So, the second direct child of this [PsiElement] is checked only
 * when the first child and all its descendants are checked.
 *
 * @throws [IllegalStateException] if this [PsiElement] does not contain such an element.
 */
public fun PsiElement.getFirstByText(
    startsWith: String,
    contains: String = startsWith
): PsiElement =
    findFirstByText(startsWith, contains)
        ?: run {
            val msg = """
            A child PSI element could not be found.
            File: ${this.containingFile.name}
            Parent element: `$this`
            Code:
            ```
            """.ti() +
            Separator.nl() +
            node.text +
            Separator.nl() +
            """
            ```            
            Search criteria: [startsWith=`$startsWith`, contains=`$contains`].                            
            """.ti()

            error(msg)
        }
