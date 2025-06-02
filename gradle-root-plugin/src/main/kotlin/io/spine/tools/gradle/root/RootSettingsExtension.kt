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

package io.spine.tools.gradle.root

import io.spine.tools.gradle.DslSpec
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.Nested

/**
 * The extension added to [Settings][org.gradle.api.initialization.Settings] of
 * a Gradle project by [SettingsPlugin].
 */
public abstract class RootSettingsExtension : ExtensionAware {

    /**
     * Allows specifying versions of dependencies that are automatically
     * added by the Gradle plugins of Spine SDK.
     */
    @get:Nested
    public abstract val versions: Versions

    /**
     * Allows configuring versions via the action block.
     */
    public fun versions(action: Versions.() -> Unit) {
        action(versions)
    }

    public companion object {

        /**
         * The name of the settings extension.
         */
        public const val NAME: String = "spineSettings"

        public val dslSpec: DslSpec<RootSettingsExtension> by lazy {
            DslSpec(NAME, RootSettingsExtension::class)
        }
    }
}
