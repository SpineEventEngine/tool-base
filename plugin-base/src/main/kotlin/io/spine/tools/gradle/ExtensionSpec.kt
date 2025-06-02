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

package io.spine.tools.gradle

import kotlin.reflect.KClass
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

/**
 * The specification of the extension added to an [ExtensionAware] instance.
 *
 * @param E The type of the extension.
 *
 * @param name The name of the extension as it appears under
 *   the [parent DSL][findOrCreateIn] element.
 * @param extensionClass The class of the extension.
 * @see findOrCreateIn
 */
public open class ExtensionSpec<E : Any>(
    public val name: String,
    public val extensionClass: KClass<E>
) {
    /**
     * Creates an extension under the given [ExtensionAware] instance,
     * if the extension is not already available.
     * 
     * The function 
     *
     * @return the newly created extension, or the one that already exists.
     */
    public fun findOrCreateIn(parent: ExtensionAware): E {
        @Suppress("UNCHECKED_CAST") // The type is ensured by the creation code below.
        val existing: E? = parent.extensions.findByName(name) as E?
        val ext = existing ?: createIn(parent.extensions)
        return ext
    }

    /**
     * Creates an extension in the given [container] using the [name] and [extensionClass].
     */
    protected open fun createIn(container: ExtensionContainer): E {
        return container.create(name, extensionClass.java)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExtensionSpec<*>) return false

        if (name != other.name) return false
        if (extensionClass != other.extensionClass) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + extensionClass.hashCode()
        return result
    }

    override fun toString(): String {
        return "ExtensionSpec(name='$name', extensionClass=$extensionClass)"
    }
}
