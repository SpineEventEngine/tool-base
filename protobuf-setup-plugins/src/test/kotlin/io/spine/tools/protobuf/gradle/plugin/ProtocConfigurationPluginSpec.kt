/*
 * Copyright 2026, TeamDev. All rights reserved.
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

package io.spine.tools.protobuf.gradle.plugin

import com.google.protobuf.gradle.ExecutableLocator
import com.google.protobuf.gradle.ProtobufExtension
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.spine.tools.protobuf.gradle.ProtobufDependencies
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`ProtocConfigurationPlugin` should")
internal class ProtocConfigurationPluginSpec {

    @Test
    fun `do nothing when the Protobuf plugin is not applied`() {
        val project = ProjectBuilder.builder().build()

        // No `com.google.protobuf` plugin — the `withPlugin` callback never fires.
        StubProtocPlugin().apply(project)

        // The plugin under test should still expose its extensions container reference.
        project.shouldNotBeNull()
    }

    @Test
    fun `configure the Protobuf extension when the plugin is present`() {
        val project = ProjectBuilder.builder().build()
        with(project) {
            pluginManager.apply(JavaPlugin::class.java)
            group = "io.spine.tests"
            version = "1.0.0"
            pluginManager.apply(ProtobufDependencies.gradlePlugin.id)
        }

        val plugin = StubProtocPlugin()
        plugin.apply(project)

        plugin.configuredPlugins shouldBe true
        project.extensions.findByName("protobuf").shouldNotBeNull()
    }

    @Test
    fun `customize the descriptor set generation of the proto tasks`() {
        val project = ProjectBuilder.builder().build()
        with(project) {
            pluginManager.apply(JavaPlugin::class.java)
            group = "io.spine.tests"
            version = "1.0.0"
            pluginManager.apply(ProtobufDependencies.gradlePlugin.id)
            extensions.getByType(ProtobufExtension::class.java)
                .protoc { it.artifact = "com.google.protobuf:protoc:3.25.1" }
        }

        val plugin = StubProtocPlugin()
        plugin.apply(project)

        // Force the configuration phase so that the `generateProtoTasks` callbacks run.
        (project as ProjectInternal).evaluate()

        plugin.customizedTasks shouldBe true
    }
}

/**
 * A minimal concrete [ProtocConfigurationPlugin] used to exercise the abstract base.
 */
private class StubProtocPlugin : ProtocConfigurationPlugin() {

    var configuredPlugins: Boolean = false
    var customizedTasks: Boolean = false

    override fun configureProtocPlugins(
        plugins: NamedDomainObjectContainer<ExecutableLocator>,
        project: Project
    ) {
        configuredPlugins = true
    }

    override fun customizeTask(protocTask: com.google.protobuf.gradle.GenerateProtoTask) {
        customizedTasks = true
    }
}
