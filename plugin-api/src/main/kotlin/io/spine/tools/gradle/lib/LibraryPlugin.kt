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

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper
import io.spine.tools.gradle.root.SpinePlugin
import io.spine.tools.gradle.root.SpineProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import io.spine.tools.gradle.root.rootExtension
import org.gradle.kotlin.dsl.apply

/**
 * The abstract base for Gradle plugins of libraries that need to
 * introduce custom extensions in [SpineProjectExtension].
 *
 * @param E The type of the extension used by the plugin.
 *  If a derived plugin class does not use an extension please pass [Unit]
 *  as the generic argument, and `null` for  the [extensionSpec] property.
 *
 * @property extensionSpec If provided, describes the extension to be added to
 *   the [root extension][io.spine.tools.gradle.root.SpineProjectExtension] by the plugin.
 */
public abstract class LibraryPlugin<E : Any>(
    private val extensionSpec: ExtensionSpec<E>?
) : Plugin<Project> {

    /**
     * The project to which this plugin is [applied][apply].
     *
     * Accessing this property before the [apply] function is called will
     * case [UninitializedPropertyAccessException].
     */
    protected val project: Project
        get() = _project

    /**
     * The backing field for the [project] property.
     */
    private lateinit var _project: Project

    /**
     * Obtains the extension added, if any, by the plugin.
     *
     * This property is `null` if the plugin does not support an extension, or,
     * if the plugin does support an extension before the [apply] function is called.
     */
    protected val extension: E?
        get() = if (this::_extension.isInitialized) _extension else null

    /**
     * The backing property for the [extension] added to the project by the plugin.
     *
     * If the plugin does not support an extension, this property is never initialized.
     */
    private lateinit var _extension: E

    /**
     * Applies [SpinePlugin] to the [project] and adds the [extension][extensionSpec]
     * if it is used by the plugin.
     *
     * Since [org.gradle.api.plugins.PluginManager.apply] does nothing
     * if a plugin is already applied, it is safe to perform the call
     * without any previous checks.
     */
    @OverridingMethodsMustInvokeSuper
    override fun apply(project: Project) {
        _project = project
        // Make sure the root extension is installed.
        project.apply<SpinePlugin>()
        extensionSpec?.let {
            _extension = project.rootExtension.extensions.create(it.name, it.extensionClass.java)
        }
    }
}
