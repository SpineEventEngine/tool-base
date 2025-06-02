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
import io.spine.tools.gradle.ExtensionSpec
import io.spine.tools.gradle.root.RootSettingsExtension
import io.spine.tools.gradle.root.SettingsPlugin
import io.spine.tools.gradle.root.hasRootExtension
import io.spine.tools.gradle.root.rootExtension
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply

/**
 * The abstract base for Gradle plugins of libraries that need to introduce
 * custom extensions in [RootSettingsExtension].
 *
 * @param E The type of the extension used by the plugin.
 *  If a derived plugin class does not use an extension please pass [Unit]
 *  as the generic argument, and `null` for the [extensionSpec] property.
 *
 * @property extensionSpec If provided, describes the extension to be added to
 *   the [root extension][io.spine.tools.gradle.root.RootExtension] by the plugin.
 */
public abstract class LibrarySettingsPlugin<E : Any>(
    private val extensionSpec: ExtensionSpec<E>?
) : Plugin<Settings> {

    /**
     * Verifies if the target [settings] have the [RootSettingsExtension] and if not,
     * applies [SettingsPlugin] so that the extension is created.
     */
    @OverridingMethodsMustInvokeSuper
    override fun apply(settings: Settings) {
        if (!settings.hasRootExtension) {
            settings.apply<SettingsPlugin>()
        }
        extensionSpec?.findOrCreateIn(settings.rootExtension)
    }
}
