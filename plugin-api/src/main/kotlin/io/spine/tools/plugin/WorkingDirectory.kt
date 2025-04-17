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

package io.spine.tools.plugin

import java.nio.file.Path
import kotlin.io.path.exists

/**
 * A working directory for a plugin.
 *
 * @param parent The path to the parent directory.
 * @param plugin The ID of the plugin which this working directory serves.
 */
public open class WorkingDirectory(
    public val parent: Path,
    public val plugin: PluginId) {

    /**
     * The path to the working directory.
     */
    public val path: Path by lazy {
        parent.resolve(plugin.value)
    }

    /**
     * Tells if the working directory exists.
     */
    public val exists: Boolean
        get() = path.exists()

    /**
     * Creates the working directory if it does not exist yet.
     *
     * @return `true` if the directory was successfully created, or if it existed
     *  prior to the call to this function.
     */
    public fun create(): Boolean {
        if (!path.exists()) {
            return path.toFile().mkdirs()
        }
        return true
    }
}
