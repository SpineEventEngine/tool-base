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

package io.spine.tools.psi

import com.intellij.openapi.editor.Document
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement

/**
 * Obtains the virtual file to which this element belongs.
 */
public val PsiElement.virtualFile: VirtualFile
    get() {
        val vf = containingFile.virtualFile
        check(vf != null) {
            "Unable to obtain a virtual file for `${containingFile.name}`."
        }
        return vf
    }

/**
 * Obtains the document to which this element belongs.
 */
public val PsiElement.document: Document
    get() {
        val doc = containingFile.document
        check(doc != null) {
            "Unable to obtain a document for `${virtualFile.name}`."
        }
        return doc
    }

/**
 * Adds the given [element] before the current
 * [firstChild][com.intellij.psi.PsiElement.getFirstChild].
 *
 * @return the added element, which could be the given instance or its copy.
 */
public fun PsiElement.addFirst(element: PsiElement): PsiElement {
    return addBefore(element, firstChild)
}
