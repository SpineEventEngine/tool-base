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

package io.spine.tools.gradle.project

import io.spine.tools.gradle.AbstractPlugin
import io.spine.tools.gradle.DslSpec
import org.gradle.api.Project

/**
 * The abstract base for Gradle project plugins.
 *
 * @param E The type of the extension used by the plugin.
 *  If a derived plugin class does not use an extension please pass [Unit]
 *  as the generic argument, and `null` for  the [dslSpec] property.
 *
 * @param dslSpec The specification of the DSL extension added by the plugin, or `null`
 *   if the plugin does not extend the project or its extensions.
 */
public abstract class ProjectPlugin<E : Any>(
    dslSpec: DslSpec<E>?
) : AbstractPlugin<Project, E>(dslSpec) {

    /**
     * The project to which this plugin is [applied][apply].
     */
    protected val project: Project
        get() = target

    /**
     * Overrides to rename the parameter.
     */
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun apply(project: Project) {
        super.apply(project)
    }
}
