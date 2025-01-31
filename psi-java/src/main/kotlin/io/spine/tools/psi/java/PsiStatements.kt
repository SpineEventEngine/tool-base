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

import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory

/**
 * A list of statements extracted from the given [PsiCodeBlock],
 * excluding the surrounding braces.
 *
 * The presence of this type addresses several challenges:
 *
 * 1. In PSI, a code block cannot be created without curly braces. As a result, even if
 *    you only need a list of statements, you must create a full block with braces.
 * 2. Inserting such a block into another code block requires range copying
 *    to skip the braces.
 * 3. While a list of statements can be [retrieved][PsiCodeBlock.getStatements] directly from
 *    the code block, formatting elements like whitespace and newlines are omitted,
 *    leaving only the raw statements. Adding these statements to a method body may
 *    result in non-compilable Java code.
 */
public class PsiStatements(codeBlock: PsiCodeBlock) {

    /**
     * The underlying [PsiCodeBlock], which actually manages children
     * of this [PsiStatements].
     *
     * PSI elements are linked to each other, and they cannot be managed
     * separately without their parents, children and siblings.
     * As a result, instead of copying only the children of interest from
     * the passed block, we have to copy the whole block using [PsiElement.copy].
     * Having a hard copy of the passed block, we can safely use its children.
     */
    private val delegate: PsiCodeBlock = codeBlock.copy() as PsiCodeBlock

    /**
     * Returns the first child of this element.
     */
    public val firstChild: PsiElement
        get() {
            check(delegate.children.size > 2) {
                "Cannot provide the first child of `PsiStatements` because it is empty!"
            }
            return delegate.children
                .first().nextSibling
        }

    /**
     * Returns the last child of this element.
     */
    public val lastChild: PsiElement
        get() {
            check(delegate.children.size > 2) {
                "Cannot provide the last child of `PsiStatements` because it is empty!"
            }
            return delegate.children
                .last().prevSibling
        }

    /**
     * Adds the given [statements] in the beginning of this [PsiStatements].
     */
    public fun append(statements: PsiStatements): Unit =
        delegate.addAfter(statements, lastChild)

    /**
     * Adds the given [statements] in the end of this [PsiStatements].
     */
    public fun prepend(statements: PsiStatements): Unit =
        delegate.addBefore(statements, firstChild)
}

/**
 * Creates a new [PsiStatements] from the given [text].
 *
 * @param text The text of the statements to create.
 * @param context The PSI element used as context for resolving references.
 */
public fun PsiElementFactory.createStatementsFromText(
    text: String,
    context: PsiElement?
): PsiStatements {
    val codeBlock = createCodeBlockFromText("{$text}", context)
    return PsiStatements(codeBlock)
}

/**
 * Adds the given [statements] to this [PsiElement].
 */
public fun PsiElement.add(statements: PsiStatements) {
    addRange(statements.firstChild, statements.lastChild)
}

/**
 * Adds the given [statements] to this [PsiElement] after the [anchor].
 */
public fun PsiElement.addAfter(statements: PsiStatements, anchor: PsiElement) {
    addRangeAfter(statements.firstChild, statements.lastChild, anchor)
}

/**
 * Adds the given [statements] to this [PsiElement] before the [anchor].
 */
public fun PsiElement.addBefore(statements: PsiStatements, anchor: PsiElement) {
    addRangeBefore(statements.firstChild, statements.lastChild, anchor)
}
