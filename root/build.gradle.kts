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

import io.spine.dependency.lib.Protobuf

plugins {
    module
    `kotlin-dsl`
    `plugin-publish`
    `write-manifest`
}

publishing.publications.withType<MavenPublication>().all {
    groupId = "io.spine.tools"
    artifactId = "spine-root-gradle-plugins"
}

gradlePlugin {
    website.set("https://spine.io/")
    vcsUrl.set("https://github.com/SpineEventEngine/tool-base.git")
    plugins {
        val pluginPackage = "io.spine.tools.gradle.root"
        val projectPluginTags = listOf(
            "spine",
            "ddd",
            "cqrs",
            "event-sourcing",
            "code-generation",
            "codegen",
            "kotlin",
            "java"
        )
        
        create("spineRootPlugin") {
            // Make sure it matches the value of the property `SpinePlugin.Companion.ID`.
            id = "io.spine.root"
            implementationClass = "$pluginPackage.SpinePlugin"
            displayName = "Spine Root Extension Plugin"
            description = "Adds the extension called `spine` in a project based on" +
                    " the SpineEventEngine SDK." +
                    " The extension will be used by the SDK components for adding own extensions."
            tags.set(projectPluginTags)
        }

        create("spineSettingsPlugin") {
            // Make sure it matches the value of the property `SpineSettingsPlugin.Companion.ID`.
            id = "io.spine.settings"
            implementationClass = "$pluginPackage.SpineSettingsPlugin"
            displayName = "Spine Settings Plugin"
            description = "Adds the extension called `spineSettings` to" +
                    " the settings of a project based on the SpineEventEngine SDK." +
                    " The extension will be used by the SDK components for adding own extensions."
            tags.set(projectPluginTags + "settings")
        }
    }
}

dependencies {
    compileOnlyApi(gradleApi())
    compileOnlyApi(gradleKotlinDsl())

    implementation(Protobuf.javaLib)?.because("""
        We need the `Message` interface for conversion of compilation settings that
        would be passed to Spine Compiler plugins.
        """.trimIndent()
    )

    testImplementation(gradleTestKit())
    testImplementation(gradleKotlinDsl())
    testImplementation(project(":plugin-base"))
    testImplementation(project(":plugin-testlib"))
}
