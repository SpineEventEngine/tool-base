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

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import java.io.File
import java.time.Instant
import java.util.*

/**
 * Utilities for converting Java source code into [PsiJavaFile].
 */
public class Parser(public val project: Project) {

    private val fileFactory by lazy {
        PsiFileFactory.getInstance(project)
    }

    /**
     * Creates a new instance of [PsiJavaFile] with the given code and auto-generated name.
     *
     * The file is not saved to the disk.
     *
     * @param javaSource The previously loaded source code.
     * @param file The file to use when creating [PsiJavaFile] instance.
     *    If `null` a synthetic file name will be used.
     */
    public fun parse(javaSource: String, file: File? = null): PsiJavaFile {
        return fromFromCode(javaSource, file)
    }

    /**
     * Creates a new instance of `PsiJavaFile` with the given content, auto-generated name, and
     * the current time as the modification timestamp.
     *
     * The instance also has the event system enabled to allow obtaining `VirtualFile` instance.
     */
    private fun fromFromCode(javaSource: String, file: File?): PsiJavaFile {
        val fileName = file?.canonicalPath ?: "__to_parse_${UUID.randomUUID()}__$FILE_SUFFIX"
        val fromFile = file?.lastModified() ?: 0
        val modificationStamp = if (fromFile == 0L) Instant.now().toEpochMilli() else fromFile
        val psiFile = fileFactory.createFileFromText(
            fileName,
            JavaFileType.INSTANCE,
            javaSource,
            modificationStamp,
            true /* `eventSystemEnabled` */
        )
        return psiFile as PsiJavaFile
    }

    internal companion object {
        const val FILE_SUFFIX = ".java"
    }
}
