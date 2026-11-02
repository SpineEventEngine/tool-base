/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package io.spine.tools.psi.java

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import io.spine.tools.psi.convertLineSeparators
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
     * Line separators of [javaSource] are normalized to `\n`, which is the only
     * separator PSI accepts. So, the code read from a file created under Windows
     * can be passed as is. Text offsets in the resulting file refer to
     * the normalized code.
     *
     * @param javaSource The previously loaded source code.
     * @param file The file to use when creating [PsiJavaFile] instance.
     *    If `null` a synthetic file name will be used.
     */
    public fun parse(javaSource: String, file: File? = null): PsiJavaFile {
        return fromFromCode(javaSource.convertLineSeparators(), file)
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
