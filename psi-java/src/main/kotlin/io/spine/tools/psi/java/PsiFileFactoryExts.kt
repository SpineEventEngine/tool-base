/*
 * Copyright 2024, TeamDev. All rights reserved.
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

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import io.spine.string.containsAny
import java.time.Instant

/**
 * Creates a Java file with the top-level class with the given name.
 *
 * @param packageName The package to which the class belongs. Could be an empty string.
 * @param className The simple name of the class.
 */
public fun PsiFileFactory.createJavaFile(
    packageName: String,
    className: String,
    publicClass: Boolean = false,
    finalClass: Boolean = false,
    modificationStamp: Instant = Instant.now()
): PsiJavaFile {
    require(className.isNotBlank()) {
        "The class name must not be blank. Encountered: `$className`."
    }
    require(className.containsAny('.', '$').not()) {
        "The class name must be simple. Encountered: `$className`."
    }
    val packageBlock =
        if (packageName.isBlank()) "" else "package $packageName;\n\n"
    val modifiers =
        (if (publicClass) "public " else "") + (if (finalClass) "final " else "")
    val code =
        packageBlock +
        """
        ${modifiers}class $className {
        }    
        """.trimIndent()
    val psiFile = createFileFromText(
        "$className.java",
        JavaFileType.INSTANCE,
        code,
        modificationStamp.toEpochMilli(),
        true /* `eventSystemEnabled` */
    ) as PsiJavaFile
    return psiFile
}
