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
import io.spine.dependency.local.Base
import io.spine.dependency.local.Logging
import io.spine.gradle.isSnapshot
import io.spine.gradle.publish.SpinePublishing
import io.spine.gradle.report.license.LicenseReporter

plugins {
    `uber-jar-module`
    `plugin-publish`
    id("io.spine.artifact-meta")
}
LicenseReporter.generateReportIn(project)

val spinePublishing = the<SpinePublishing>()

artifactMeta {
    artifactId.set(spinePublishing.artifactId(project))
    addDependencies(
        // Add `protoc` and Gradle Plugin as explicit dependencies as we use them
        // when configuring test projects.
        Protobuf.compiler,
        Protobuf.GradlePlugin.lib
    )
}

// As defined in `versions.gradle.kts`.
val versionToPublish: String by extra

description = "Utilities for working with Protobuf projects under Gradle."

kotlin {
    explicitApi()
}

gradlePlugin {
    website.set("https://spine.io/")
    vcsUrl.set("https://github.com/SpineEventEngine/tool-base.git")
    plugins {
        val commonTags = listOf("protobuf", "gradle")
        create("descriptorSetFilePlugin") {
            id = "io.spine.descriptor-set-file"
            implementationClass = "io.spine.tools.protobuf.gradle.plugin.DescriptorSetFilePlugin"
            displayName = "Descriptor Set File Plugin"
            description = "Configures `GenerateProtoTask`s to produce descriptor set files and load them as resources."
            tags.set(commonTags + listOf("descriptor", "resources"))
        }
        create("generatedSourcePlugin") {
            id = "io.spine.generated-sources"
            implementationClass = "io.spine.tools.protobuf.gradle.plugin.GeneratedSourcePlugin"
            displayName = "Generated Source Plugin"
            description = "Redirects Protobuf compilation to `$projectDir/generated`."
            tags.set(commonTags + listOf("java", "kotlin", "sources"))
        }
    }
}

// Do not publish to Gradle Plugin Portal snapshot versions.
// It is prohibited by their policy: https://plugins.gradle.org/docs/publish-plugin
val publishPlugins: Task by tasks.getting {
    enabled = !versionToPublish.isSnapshot()
}

@Suppress("unused")
val publish: Task by tasks.getting {
    dependsOn(publishPlugins)
}

dependencies {
    compileOnlyApi(gradleApi())
    compileOnly(gradleKotlinDsl())

    // Access to `GenerateProtoTask` and related APIs at compile and runtime.
    implementation(Protobuf.GradlePlugin.lib)

    implementation(Base.lib)?.because("We need `DescriptorSetReferenceFile` at compile time.")
    implementation(project(":plugin-base"))
    implementation(Logging.lib)
    
    testImplementation(project(":plugin-testlib"))
}

publishing.publications.withType<MavenPublication>().configureEach {
    when (name) {
        "fatJar" -> {
            // Avoid the conflict with the `pluginMaven` publication.
            artifactId = "protobuf-setup-plugins-all"
        }
    }
}
