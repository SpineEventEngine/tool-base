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

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

/**
 * Obtains [CodeStyleManager] for this project.
 */
public val Project.codeStyleManager: CodeStyleManager
    get() = CodeStyleManager.getInstance(this)

/**
 * Obtains code settings for this project.
 */
public val Project.codeStyleSettings: CodeStyleSettings
    get() = CodeStyle.getSettings(this)

/**
 * Obtains custom code settings of the type [T] from this [CodeStyleSettings].
 */
public inline fun <reified T: CustomCodeStyleSettings> CodeStyleSettings.get(): T {
    return getCustomSettings(T::class.java)
}

/**
 * Assigns main project-wide code settings to be used instead of application-wide.
 *
 * @see CodeStyle.setMainProjectSettings
 */
public fun Project.force(settings: CodeStyleSettings) {
    CodeStyle.setMainProjectSettings(this, settings)
}

/**
 * Obtains a document manager for this project.
 */
public val Project.documentManager: PsiDocumentManager
    get() = PsiDocumentManager.getInstance(this)
