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

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.io.File
import java.nio.file.Path

/**
 * An abstract base for classes implementing file operations.
 *
 * @param F
 *         the type of the [PsiFile] objects implementing classed deal with.
 * @param project
 *         the project to which files belong.
 * @param suffix
 *         the extension of files managed by this file system, or
 *         an empty string if all kinds of files can be handled.
 *         The suffix should match the type `F` of `PsiFile` objects.
 *         For example, `PsiJavaFile` deals with `.java` files.
 */
public abstract class AbstractFileSystem<F: PsiFile>(
    protected val project: Project,
    suffix: String,
) {

    private val psiManager by lazy {
        PsiManager.getInstance(project)
    }

    private val localFileSystem by lazy {
        CoreLocalFileSystem()
    }

    /**
     * The extensions of files
     */
    public val suffix: String = suffix.ensurePrefix(".")

    /**
     * Locates the file on the local file system and creates
     * corresponding [PsiFile] instance cast to [F].
     *
     * @throws IllegalArgumentException
     *         — If the file has an extension other than ".java".
     *         — If the file does not exist.
     * @throws IllegalStateException
     *          — If [CoreLocalFileSystem] could not obtain [VirtualFile].
     *          — If [PsiManager] could not find the virtual file.
     */
    public fun load(file: File): F {
        if (suffix.isNotEmpty()) {
            require(file.toString().endsWith(suffix)) {
                "The file `$file` must have the `$suffix` extension."
            }
        }
        require(file.exists()) {
            "The file `$file` does not exist."
        }
        val found = localFileSystem.findFileByIoFile(file)
        check(found != null) {
            "Unable to locate the file `$file` in the local file system."
        }
        psiManager.findFile(found)?.let {
            @Suppress("UNCHECKED_CAST")
            return it as F
        }
        error("Unable to load the file `$file`.")
    }

    /**
     * Same as [load], but accepting [Path] instead of [File].
     */
    public fun load(file: Path): F = load(file.toFile())
}

private fun String.ensurePrefix(prefix: String): String {
    if (isEmpty() || startsWith(prefix)) {
        return this
    }
    return prefix + this
}
