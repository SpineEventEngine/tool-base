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

import io.spine.string.simpleClassName
import kotlin.reflect.KClass
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

/**
 * The specification of an extension added to an [ExtensionAware] instance.
 *
 * An instance of this class helps a Gradle plugin that need to
 * introduce a custom extension to a [project][org.gradle.api.Project] or
 * another [ExtensionAware] instance for providing DSL for tuning features
 * offered by the plugin.
 *
 * Authors of the Gradle plugin class should call the [findOrCreateIn] function
 * passing appropriate [ExtensionAware] under which the extension will be created.
 * Presumably, the call to [findOrCreateIn] should be made in
 * the [apply][org.gradle.api.Plugin.apply] function of the plugin or shortly
 * _after_ the plugin is applied.
 *
 * @param E The type of the extension.
 *
 * @param name The name of the extension as it appears under
 *   the [parent DSL][findOrCreateIn] element.
 * @param extensionClass The class of the extension.
 * @see findOrCreateIn
 */
public open class DslSpec<E : Any>(
    public val name: String,
    public val extensionClass: KClass<E>
) {
    /**
     * Obtains an instance of the extension from the given DSL parent object.
     *
     * If the extension is already [available][ExtensionContainer.findByName] by its [name],
     * it is returned.
     *
     * Otherwise, a new extension is [created][createIn], and its instance is returned.
     * 
     * @return the newly created extension, or the one that already exists.
     */
    public fun findOrCreateIn(parent: ExtensionAware): E {
        val container = parent.extensions
        @Suppress("UNCHECKED_CAST") // The type is ensured by the creation code below.
        val existing: E? = container.findByName(name) as E?
        val ext = existing ?: createIn(container)
        return ext
    }

    /**
     * Creates an extension in the given [container] using the [name] and the [extensionClass].
     *
     * The default implementation uses the [ExtensionContainer.create] method which accepts
     * the name and the Java counterpart of the [extensionClass].
     *
     * Overriding classes may use other methods offered by the [ExtensionContainer] interface
     * to tailor the extension creation for the needs of the served plugin.
     */
    protected open fun createIn(container: ExtensionContainer): E {
        return container.create(name, extensionClass.java)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DslSpec<*>) return false

        if (name != other.name) return false
        if (extensionClass != other.extensionClass) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + extensionClass.hashCode()
        return result
    }

    /**
     * Obtains diagnostic representation of the DSL specification.
     */
    override fun toString(): String {
        return "$simpleClassName(name='$name', extensionClass=$extensionClass)"
    }
}
