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
import io.spine.tools.gradle.root.hasRootExtension
import io.spine.tools.gradle.root.rootExtension
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("`LibraryPlugin` should")
internal class LibraryPluginSpec {

    private val rootPlugin = RootPlugin::class.java

    private lateinit var project: Project
    private lateinit var plugin: StubPlugin

    @BeforeEach
    fun createProject() {
        project = ProjectBuilder.builder().build()
        plugin = StubPlugin()
    }

    /**
     * This test ensures that plugins extending [LibraryPlugin] can work together
     * assuming the fact that [LibraryPlugin] automatically applies [RootPlugin] if
     * it is not yet allied to the project.
     */
    @Test
    fun `support applying descending plugin classes`() {
        assertDoesNotThrow {
            plugin.apply(project)
            AnotherStubPlugin().apply(project)
        }
    }

    @Test
    fun `apply 'SpinePlugin' if it is not applied yet`() {
        project.plugins.findPlugin(rootPlugin) shouldBe null

        plugin.apply(project)

        project.plugins.findPlugin(rootPlugin) shouldNotBe null
    }

    @Test
    fun `apply to a project if 'SpinePlugin' is already applied`() {
        project.pluginManager.apply(rootPlugin)
        assertDoesNotThrow {
            project.pluginManager.apply(StubPlugin::class.java)
        }
    }

    @Test
    fun `remember the project to which it is applied`() {
        assertThrows<UninitializedPropertyAccessException> {
            plugin.project()
        }
        plugin.apply(project)
        plugin.project() shouldBe project
    }

    @Test
    fun `obtain root extension after the plugin is applied to the project`() {
        plugin.run {
            hasRootExtension(project) shouldBe false
            assertThrows<UnknownDomainObjectException> {
                rootExtension(project)
            }

            apply(project)

            hasRootExtension(project) shouldBe true
            rootExtension(project) shouldNotBe null
        }
    }
}

private class StubPlugin : LibraryPlugin<Unit>(null) {

    fun project() = project
    fun hasRootExtension(project: Project) = project.hasRootExtension
    fun rootExtension(project: Project) = project.rootExtension
}

private class AnotherStubPlugin : LibraryPlugin<Unit>(null)
