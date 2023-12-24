/*
 * Copyright 2023, TeamDev. All rights reserved.
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

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import java.io.File
import java.time.Instant
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.psi.PsiManager
import java.util.*

/**
 * Utilities for converting Java source code into [PsiJavaFile].
 */
public class Parser(public val project: Project) {

    private val fileFactory by lazy {
        PsiFileFactory.getInstance(project)
    }

    private val localFileSystem by lazy {
        CoreLocalFileSystem()
    }

    private val psiManager by lazy {
        PsiManager.getInstance(project)
    }

    /**
     * Creates a new instance of [PsiJavaFile] with the given code and auto-generated name.
     *
     * The file is not saved to the disk.
     */
    public fun parse(javaSource: String): PsiJavaFile =
        syntheticFile(javaSource)

    /**
     * Parses the given file and returns the corresponding [PsiJavaFile].
     */
    public fun parse(file: File): PsiJavaFile {
        require(file.toString().endsWith(FILE_SUFFIX)) {
            "The file `$file` must have the `$FILE_SUFFIX` extension."
        }
        require(file.exists()) {
            "The file `$file` does not exist."
        }
        val found = localFileSystem.findFileByIoFile(file)
        require(found != null) {
            "Unable to locate the file `$file` in the local file system."
        }
        psiManager.findFile(found)?.let {
            return it as PsiJavaFile
        }
        error("Unable to parse the file `$file`.")
    }

    /**
     * Creates a new instance of `PsiJavaFile` with the given content, auto-generated name, and
     * the current time as the modification timestamp.
     *
     * The instance also has the event system enabled to allow obtaining `VirtualFile` instance.
     */
    private fun syntheticFile(javaSource: String): PsiJavaFile {
        val fileName = "__to_parse_${UUID.randomUUID()}__$FILE_SUFFIX"
        val modificationStamp = Instant.now().toEpochMilli()
        val file = fileFactory.createFileFromText(
            fileName,
            JavaFileType.INSTANCE,
            javaSource,
            modificationStamp,
            true /* `eventSystemEnabled` */
        )
        return file as PsiJavaFile
    }

    private companion object {
        const val FILE_SUFFIX = ".java"
    }
}
