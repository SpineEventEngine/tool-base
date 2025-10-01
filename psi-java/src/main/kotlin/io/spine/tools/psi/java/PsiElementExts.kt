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

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import io.spine.annotation.VisibleForTesting
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

/**
 * Obtains the whitespace-normalized code of this [PsiElement].
 *
 * The function transforms the code obtained from the [text][PsiElement.getText] property
 * in the following way:
 *  1. drops comments,
 *  2. collapses any whitespace run to a single space character,
 *  3. *avoids spaces* around punctuation where code style normally has none
 *     (before `, ) ] } . :: ?. ; >` and after `( [ {` etc.).
 */
@Suppress("AssignedValueIsNeverRead" /* False positive from IDEA. The `needSpace` var
    is used when calculating `addSpace` var. */,
    "ReturnCount"
)
@VisibleForTesting
public fun PsiElement.canonicalCode(): String {
    val sb = StringBuilder()
    var needSpace = false

    fun lastChar(): Char? = if (sb.isEmpty()) null else sb[sb.length - 1]

    val noBefore = charSet(",;:).]?") // includes ., ::, ?., ?:
    val noAfter = charSet("([.<")

    // No space BEFORE these leading chars
    fun noSpaceBefore(next: String): Boolean =
        next == "++" || next == "--" ||
            next.firstOrNull() in noBefore

    // No space AFTER tokens that end with these trailing chars
    fun noSpaceAfter(prevLast: Char?): Boolean = prevLast in noAfter

    accept(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(e: PsiElement) {
            when (e) {
                is PsiWhiteSpace -> {
                    needSpace = true
                    return
                }
                is PsiComment -> {
                    // drop comments entirely
                    needSpace = true
                    return
                }
            }

            // Emit only leaf tokens’ text. (Composite nodes delegate to children.)
            if (e.firstChild == null && e is LeafPsiElement) {
                val text = e.text
                if (text.isBlank()) {
                    needSpace = true
                    return
                }

                val addSpace =
                    needSpace &&
                            !noSpaceBefore(text) &&
                            !noSpaceAfter(lastChar())

                if (addSpace) sb.append(' ')
                sb.append(text)
                needSpace = false
                return
            }

            super.visitElement(e)
        }
    })

    return sb.toString().trim()
}

private fun charSet(chars: String): Set<Char> = chars.toSet()

