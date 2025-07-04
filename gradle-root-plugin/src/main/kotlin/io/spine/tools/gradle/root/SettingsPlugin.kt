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

import io.spine.tools.gradle.AbstractPlugin
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware

/**
 * Adds [spineSettings][RootSettingsExtension] extension in the [Settings]
 * to which the plugin is applied.
 *
 * Before adding the extension, the plugin checks for the present of the extension.
 * So, applying the plugin more than once has no effect.
 */
public class SettingsPlugin : AbstractPlugin<Settings, RootSettingsExtension>(
    RootSettingsExtension.dslSpec
) {
    override val dslParent: ExtensionAware?
        get() = target

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun apply(settings: Settings) {
        super.apply(settings)
        createExtension()
    }

    public companion object {

        /**
         * The ID of the plugin.
         */
        public const val ID: String = "io.spine.settings"
    }
}
