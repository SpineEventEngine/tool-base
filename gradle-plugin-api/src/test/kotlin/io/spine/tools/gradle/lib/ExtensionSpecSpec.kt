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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.tools.gradle.root.RootPlugin
import io.spine.tools.gradle.root.rootExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * This test suite tests only the features of the [ExtensionSpec.createIn]
 * function which accepts [Project] because obtaining an instance of
 * [Settings][org.gradle.api.initialization.Settings] is close to impossible
 * with the v8.14 of Gradle Test Kit we use.
 *
 * We are not compromising on the reliability of the code much here because:
 *  1. The main logic of finding or creating an extension in an
 *   [ExtensionAware][org.gradle.api.plugins.ExtensionAware] instance
 *   is done by [ExtensionSpec.findOrCreate] function.
 *
 *  2. Application of a [LibrarySettingsPlugin] which calls [ExtensionSpec.createIn]
 *   with the [Settings][org.gradle.api.initialization.Settings] parameter is done
 *   by [LibrarySettingsPluginSpec] test suite.
 */
@DisplayName("`ExtensionSpec` should")
internal class ExtensionSpecSpec {

    private lateinit var project: Project
    private lateinit var extensionSpec: ExtensionSpec<*>

    @BeforeEach
    fun createProject() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply(RootPlugin::class.java)
        extensionSpec = ExtensionSpec(StubExtension.NAME, StubExtension::class)
    }

    @Test
    fun `create a new instance in the given project`() {
        extensionSpec.createIn(project)
        project.rootExtension.extensions.findByName(StubExtension.NAME) shouldNotBe null
    }

    @Test
    fun `obtain already created instance of an extension in the given project`() {
        val ext = extensionSpec.createIn(project)
        extensionSpec.createIn(project) shouldBe ext
    }
}

@Suppress("UtilityClassWithPublicConstructor") // Make `detekt` happy.
abstract class StubExtension {
    companion object {
        const val NAME = "stub"
    }
}
