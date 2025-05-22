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

package io.spine.tools.gradle.lib

import io.spine.tools.gradle.root.rootExtension
import kotlin.reflect.KClass
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionContainer

/**
 * The specification of the extension added to
 * the [root extension][io.spine.tools.gradle.root.RootExtension]
 * by a [LibraryPlugin].
 *
 * @param E The type of the extension.
 *
 * @param name The name of the extension as it appears under
 *   the [spine][io.spine.tools.gradle.root.RootExtension] block or
 *   the [spineSettings][io.spine.tools.gradle.root.RootSettingsExtension] block.
 * @param extensionClass The class of the extension.
 */
public data class ExtensionSpec<E : Any>(
    public val name: String,
    public val extensionClass: KClass<E>
) {
    /**
     * Creates an extension in under the [rootExtension][Project.rootExtension] of
     * the given project, if the extension is not already available.
     *
     * @return the newly created extension, or the one that already exists.
     */
    public fun createIn(project: Project): E {
        val ext = project.rootExtension.extensions.findOrCreate()
        return ext
    }

    /**
     * Creates an extension in under the [rootExtension][Settings.rootExtension] of
     * the given settings, if the extension is not already available.
     *
     * @return the newly created extension, or the one that already exists.
     */
    public fun createIn(settings: Settings): E {
        val ext = settings.rootExtension.extensions.findOrCreate()
        return ext
    }

    private fun ExtensionContainer.findOrCreate(): E {
        @Suppress("UNCHECKED_CAST") // The type is ensured by the creation code below.
        val existing: E? = findByName(name) as E?
        return existing ?: create(name, extensionClass.java)
    }
}

