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

import io.spine.gradle.isSnapshot

plugins {
    module
    `kotlin-dsl`
    `plugin-publish`
    `write-manifest`
}

// As defined in `versions.gradle.kts`.
val versionToPublish: String by extra

publishing.publications.withType<MavenPublication>().all {
    groupId = "io.spine.tools"
    // It's plural because there are two plugins in the JAR.
    artifactId = "spine-root-gradle-plugins"
    version = versionToPublish
}

// Do not publish to Gradle Plugin Portal snapshot versions.
// It is prohibited by their policy: https://plugins.gradle.org/docs/publish-plugin
val publishPlugins: Task by tasks.getting {
    enabled = !versionToPublish.isSnapshot()
}

val publish: Task by tasks.getting {
    dependsOn(publishPlugins)
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

    // Expose the below dependencies as `testFixturesApi` so that plugins
    // that extend root project extension and settings can use them for their testing.
    arrayOf(
        gradleTestKit(),
        gradleKotlinDsl(),
        project(":plugin-base"),
        project(":plugin-testlib")
    ).forEach {
        testFixturesApi(it)
    }
}
