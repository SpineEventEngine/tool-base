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

package io.spine.tools.gradle.jvm.plugin

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.setProperty

/**
 * An extension for configuring how artifact metadata is collected and written.
 *
 * Appears in a Gradle project as `artifactMeta`.
 */
public open class ArtifactMetaExtension(project: Project) {

    /**
     * Configuration exclusions specified via DSL.
     */
    @Nested
    public val excludeConfigurations: ExcludeConfigurations = ExcludeConfigurations(project)

    /**
     * Configures [excludeConfigurations] via an action/closure.
     */
    public fun excludeConfigurations(action: Action<ExcludeConfigurations>) {
        action.execute(excludeConfigurations)
    }

    internal companion object {

        internal const val NAME = "artifactMeta"
    }
}

/**
 * Holder for configuration exclusion rules.
 */
public open class ExcludeConfigurations(project: Project) {

    /**
     * Names of configurations to be excluded exactly as named.
     */
    public val named: SetProperty<String> = project.objects.setProperty(String::class)

    /**
     * Substrings; any configuration whose name contains one of these substrings will be excluded.
     */
    public val containing: SetProperty<String> = project.objects.setProperty(String::class)

    /**
     * Adds exact configuration names to exclude.
     */
    public fun named(vararg names: String) {
        named.addAll(*names)
    }

    /**
     * Adds parts of names of configurations to be excluded.
     *
     * Any configuration whose name contains one of these will be excluded.
     */
    public fun containing(vararg parts: String) {
        containing.addAll(*parts)
    }
}
