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

package io.spine.tools.gradle.root.plugin

import io.spine.tools.gradle.root.plugin.SpinePlugin.Companion.ROOT_WORKING_DIR_NAME
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.kotlin.dsl.getByType

/**
 * Tells if the project already has the [spine][SpineProjectExtension] extension.
 *
 * @returns `true` if the project already has the [extension][SpineProjectExtension] applied,
 *  `false` otherwise.
 * @see rootExtension
 */
public val Project.hasRootExtension: Boolean
    get() = project.extensions.findByName(SpineProjectExtension.NAME) != null

/**
 * Obtains the instance of the [spine][SpineProjectExtension] extension of the project.
 *
 * @throws org.gradle.api.UnknownDomainObjectException if the extension is not found.
 * @see hasRootExtension
 */
public val Project.rootExtension: SpineProjectExtension
    get() = extensions.getByType<SpineProjectExtension>()

/**
 * The name of the directory under the project `build` directory which
 * is used for storing temporary files of the libraries based on the Spine SDK.
 */
public val Project.rootWorkingDir: Directory
    get() = layout.buildDirectory.dir(ROOT_WORKING_DIR_NAME).get()
