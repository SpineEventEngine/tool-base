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

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper
import io.spine.string.qualifiedClassName
import org.gradle.api.Plugin
import org.gradle.api.plugins.ExtensionAware

/**
 * An abstract base for plugins extending Gradle.
 *
 * @param T The type of object which this plugin can configure.
 *
 * @param E The type of the extension used by the plugin.
 *  If a derived plugin class does not use an extension please pass [Unit]
 *  as the generic argument, and `null` for  the [dslSpec] property.
 *
 * @property dslSpec If provided, describes the extension to be added to
 *   the [dslParent] by the plugin.
 */
public abstract class AbstractPlugin<T : Any, E : Any>(
    protected val dslSpec: DslSpec<E>?
) : Plugin<T> {

    /**
     * The backing field for the [target] property.
     */
    private lateinit var _target: T

    /**
     * The target to which this plugin is applied.
     *
     * Accessing this property before the [apply] function is called will
     * case [UninitializedPropertyAccessException].
     */
    protected val target: T
        get() = _target

    /**
     * Tells if this plugin has an extension.
     */
    public val hasExtension: Boolean = dslSpec != null

    /**
     * The DSL element which this plugin extends, or `null` if it does not.
     */
    protected abstract val dslParent: ExtensionAware?

    /**
     * Obtains the extension added, if any, by the plugin.
     *
     * This property must be accessed _after_ the plugin is [applied][apply].
     * Otherwise, [IllegalStateException] will be thrown.
     *
     * This property is `null` if the plugin does not [support an extension][hasExtension].
     */
    protected val extension: E? by lazy {
        if (hasExtension) {
            check(this::_target.isInitialized) {
                "Unable to obtain an extension:" +
                        " the plugin `$qualifiedClassName` has not been applied yet."
            }
            dslSpec!!.findOrCreateIn(dslParent!!)
        } else {
            null
        }
    }

    /**
     * Ensures that the extension has been created if the plugin provides one.
     *
     * If the extension has been already created, it is returned as the result
     * of this function.
     *
     * ### API note
     * This function exists to make the code self-documented at the call sites.
     * We need to force the extension creation at some places, and the call to
     * this function is easier to understand than an expression containing
     * just the [extension] property.
     *
     * ### Custom creation of extensions
     * To create an extension in a way other than provided by the [DslSpec.createIn] function,
     * please do the following:
     *  1. Create a class derived from [DslSpec].
     *  2. Override the [DslSpec.createIn] method.
     *  3. Pass the instance of the class to the [AbstractPlugin] constructor.
     *
     * @return the extension created under the [dslParent], or
     *   `null` if the plugin does not have an extension.
     */
    protected fun createExtension(): E? = extension

    /**
     * Remembers the project.
     */
    @OverridingMethodsMustInvokeSuper
    override fun apply(target: T) {
        _target = target
    }
}
