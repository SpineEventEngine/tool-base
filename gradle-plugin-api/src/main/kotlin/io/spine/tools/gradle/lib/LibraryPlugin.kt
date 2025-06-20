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
import io.spine.tools.gradle.DslSpec
import io.spine.tools.gradle.project.ProjectPlugin
import io.spine.tools.gradle.root.RootExtension
import io.spine.tools.gradle.root.RootPlugin
import io.spine.tools.gradle.root.rootExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply

/**
 * The abstract base for Gradle plugins of libraries that need to
 * introduce custom extensions in [RootExtension].
 *
 * @param dslSpec If provided, describes the extension to be added to
 *   the [root extension][RootExtension] by the plugin.
 */
public abstract class LibraryPlugin<E : Any>(
    dslSpec: DslSpec<E>?
) : ProjectPlugin<E>(dslSpec) {

    /**
     * Returns [Project.rootExtension].
     */
    override val dslParent: ExtensionAware?
        get() = project.rootExtension

    /**
     * Applies the plugin the [project].
     *
     * The [extension], if it is introduced by the plugin is available after
     * this function is called.
     *
     * The function forces applying the [RootPlugin] so that [RootExtension] is available.
     * Since [org.gradle.api.plugins.PluginManager.apply] does nothing
     * if a plugin is already applied, it is safe to perform the call
     * without any previous checks.
     */
    @OverridingMethodsMustInvokeSuper
    override fun apply(project: Project) {
        super.apply(project)
        project.apply<RootPlugin>()
        createExtension()
    }
}
