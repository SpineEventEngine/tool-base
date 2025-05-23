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

@file:Suppress("unused") // Used via publication settings.

package io.spine.tools.gradle.lib.given

import io.spine.tools.gradle.lib.ExtensionSpec
import io.spine.tools.gradle.lib.LibraryPlugin
import io.spine.tools.gradle.root.hasRootExtension
import io.spine.tools.gradle.root.rootExtension
import org.gradle.api.Project
import org.gradle.api.provider.Property

class StubPlugin : LibraryPlugin<StubExtension>(
    ExtensionSpec(StubExtension.NAME, StubExtension::class)
) {
    fun project() = project
    fun hasRootExtension(project: Project) = project.hasRootExtension
    fun rootExtension(project: Project) = project.rootExtension
}

abstract class StubExtension {
    abstract val property: Property<String>

    companion object {
        const val NAME = "stubExtension"
    }
}

class AnotherStubPlugin : LibraryPlugin<Unit>(null)
