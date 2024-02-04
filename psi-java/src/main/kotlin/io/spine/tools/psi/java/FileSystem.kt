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

import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import io.spine.tools.psi.java.Parser.Companion.FILE_SUFFIX
import java.io.File

/**
 * File operations for working with [PsiJavaFile] instances.
 */
public object FileSystem {

    private val psiManager by lazy {
        PsiManager.getInstance(Environment.project)
    }

    private val localFileSystem by lazy {
        CoreLocalFileSystem()
    }

    /**
     * Locates the file on the local file system and creates
     * corresponding [PsiJavaFile] instance.
     *
     * @throws IllegalArgumentException
     *         — If the file has an extension other than ".java".
     *         — If the file does not exist.
     * @throws IllegalStateException
     *          — If [CoreLocalFileSystem] could not obtain [VirtualFile].
     *          — If [PsiManager] could not find the virtual file.
     */
    public fun load(file: File): PsiJavaFile {
        require(file.toString().endsWith(FILE_SUFFIX)) {
            "The file `$file` must have the `$FILE_SUFFIX` extension."
        }
        require(file.exists()) {
            "The file `$file` does not exist."
        }
        val found = localFileSystem.findFileByIoFile(file)
        check(found != null) {
            "Unable to locate the file `$file` in the local file system."
        }
        psiManager.findFile(found)?.let {
            return it as PsiJavaFile
        }
        error("Unable to load the file `$file`.")
    }
}
