/*
 * Copyright 2026, TeamDev. All rights reserved.
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

package io.spine.tools.psi

import com.intellij.psi.PsiJavaFile
import io.kotest.matchers.nulls.shouldNotBeNull
import io.spine.tools.psi.java.FileSystem
import io.spine.tools.psi.java.topLevelClass
import java.nio.file.Path
import kotlin.io.path.writeText
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Tests language-neutral [com.intellij.psi.PsiElement] extensions
 * declared in the `psi` module.
 */
@DisplayName("`PsiElement` PSI extensions should")
internal class PsiElementExtsSpec {

    /**
     * Loads a disk-backed PSI class so that its containing file has both a virtual file
     * and a document, and the call resolves to the `PsiElement` extension rather than
     * to a member of `PsiFile`.
     */
    private fun loadClass(tempDir: Path) =
        (FileSystem.load(tempDir.resolve("Stub.java").apply {
            writeText("class Stub {}")
        }) as PsiJavaFile).topLevelClass

    @Test
    fun `obtain the virtual file of an element`(@TempDir tempDir: Path) {
        loadClass(tempDir).virtualFile.shouldNotBeNull()
    }

    @Test
    fun `obtain the document of an element`(@TempDir tempDir: Path) {
        loadClass(tempDir).document.shouldNotBeNull()
    }
}
