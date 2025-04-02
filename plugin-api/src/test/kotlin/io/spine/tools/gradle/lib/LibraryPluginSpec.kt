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
import io.spine.tools.gradle.root.SpinePlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@DisplayName("`LibraryPlugin` should")
internal class LibraryPluginSpec {

    private val rootPlugin = SpinePlugin::class.java

    private lateinit var project: Project
    private lateinit var plugin: StubPlugin

    @BeforeEach
    fun createProject() {
        project = ProjectBuilder.builder().build()
        plugin = StubPlugin()
    }

    /**
     * This test ensures that plugins extending [LibraryPlugin] can work together
     * assuming the fact that [LibraryPlugin] automatically applies [SpinePlugin] if
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
}

private class StubPlugin : LibraryPlugin()
private class AnotherStubPlugin : LibraryPlugin()
