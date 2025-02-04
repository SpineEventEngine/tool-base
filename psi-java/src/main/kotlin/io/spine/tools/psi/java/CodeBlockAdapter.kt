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
 * Adapts [PsiCodeBlock] to be used without the surrounding curly braces.
 *
 * The presence of this adapter addresses several challenges:
 *
 * 1. In PSI for Java, a code block cannot be created without curly braces. As a result,
 *    even if you only need a list of statements, you must create a block with braces.
 * 2. Inserting such a block into another code block requires range copying to skip the braces.
 * 3. While a list of statements can be directly [retrieved][PsiCodeBlock.getStatements],
 *    formatting elements like whitespaces and newlines are omitted, which may lead
 *    to non-compilable Java code when added to a method body.
 */
public class CodeBlockAdapter(codeBlock: PsiCodeBlock) {

    /**
     * The underlying [PsiCodeBlock], which actually manages elements
     * in this adapter.
     *
     * PSI elements are linked with each other, and they cannot be managed
     * separately without their child-parent and sibling relations.
     * As a result, instead of copying only the children of interest from
     * the passed block, we have to copy the whole block using [PsiElement.copy].
     *
     * Having a hard copy of the passed block protects us from the outside changes
     * to the original block, also allowing PSI to manage the elements correctly
     * when modified by [append] and [prepend] methods.
     */
    private val delegate: PsiCodeBlock = codeBlock.copy() as PsiCodeBlock

    /**
     * Returns the first element after the opening curly brace `{`.
     *
     * The returned element might be a statement,
     * or a formatting element like a whitespace or a newline.
     *
     * @throws IllegalStateException if this [CodeBlockAdapter] has an empty body.
     */
    public val firstBodyElement: PsiElement
        get() = delegate.firstBodyElement
            ?: error(
                "Cannot provide the first body element of the code block " +
                        "because the block is empty."
            )

    /**
     * Returns the last element before the closing curly brace `}`.
     *
     * The returned element might be a statement,
     * or a formatting element like a whitespace or a newline.
     *
     * @throws IllegalStateException if this [CodeBlockAdapter] has an empty body.
     */
    public val lastBodyElement: PsiElement
        get() = delegate.lastBodyElement
            ?: error(
                "Cannot provide the last body element of the code block " +
                        "because the block is empty."
            )

    /**
     * Adds the given code [block] in the beginning of this [CodeBlockAdapter].
     */
    public fun append(block: CodeBlockAdapter): Unit =
        delegate.addAfter(block, lastBodyElement)

    /**
     * Adds the given code [block] in the end of this [CodeBlockAdapter].
     */
    public fun prepend(block: CodeBlockAdapter): Unit =
        delegate.addBefore(block, firstBodyElement)
}

/**
 * Creates a new [CodeBlockAdapter] from the given [text].
 *
 * @param text The text of the block to create.
 * @param context The PSI element used as context for resolving references.
 */
public fun PsiElementFactory.createCodeBlockAdapterFromText(
    text: String,
    context: PsiElement?
): CodeBlockAdapter {
    val codeBlock = createCodeBlockFromText("{$text}", context)
    return CodeBlockAdapter(codeBlock)
}

/**
 * Adds the given code [block] to this [PsiElement].
 */
public fun PsiElement.add(block: CodeBlockAdapter) {
    addRange(block.firstBodyElement, block.lastBodyElement)
}

/**
 * Adds the given code [block] to this [PsiElement] after the [anchor].
 */
public fun PsiElement.addAfter(block: CodeBlockAdapter, anchor: PsiElement) {
    addRangeAfter(block.firstBodyElement, block.lastBodyElement, anchor)
}

/**
 * Adds the given code [block] to this [PsiElement] before the [anchor].
 */
public fun PsiElement.addBefore(block: CodeBlockAdapter, anchor: PsiElement) {
    addRangeBefore(block.firstBodyElement, block.lastBodyElement, anchor)
}
