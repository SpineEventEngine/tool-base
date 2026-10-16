/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

import io.spine.dependency.lib.Kotlin
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.isSnapshot
import io.spine.gradle.publish.unbundleProtobufRuntime
import io.spine.gradle.testing.enableTestKitCoverage

plugins {
    `uber-jar-module`
    kotlin("jvm")
    `module-testing`
    `plugin-publish`
    `write-manifest`
}
LicenseReporter.generateReportIn(project)

// Credit coverage of the plugins executed in TestKit worker JVMs by the
// `*PluginSpec` tests. See `io.spine.gradle.testing.enableTestKitCoverage`.
enableTestKitCoverage()

// As defined in `versions.gradle.kts`.
val versionToPublish = extra["versionToPublish"] as String

description = "Utilities for working with JVM projects under Gradle."

kotlin {
    explicitApi()
}

publishing.publications.withType<MavenPublication>().configureEach {
    when (name) {
        "fatJar" -> {
            // Avoid the conflict with the `pluginMaven` publication.
            artifactId = "jvm-tool-plugins-all"
        }
    }
}

// Declare the Protobuf runtime as a dependency rather than bundle it,
// so that the runtime resolved by the consumer wins.
unbundleProtobufRuntime()

// Do not publish to Gradle Plugin Portal snapshot versions.
// It is prohibited by their policy: https://plugins.gradle.org/docs/publish-plugin
val publishPlugins = tasks.getByName("publishPlugins") {
    enabled = !versionToPublish.isSnapshot()
}

tasks.getByName("publish") {
    dependsOn(publishPlugins)
}

gradlePlugin {
    website.set("https://spine.io/")
    vcsUrl.set("https://github.com/SpineEventEngine/tool-base.git")
    plugins {
        val pluginTags = listOf(
            "gradle",
            "kotlin",
            "java",
            "jvm"
        )

        create("artifactMetaPlugin") {
            id = "io.spine.artifact-meta"
            implementationClass = "io.spine.tools.gradle.jvm.plugin.ArtifactMetaPlugin"
            displayName = "Spine Artifact Metadata Plugin"
            description = "Stores the artifact metadata in the resources"
            tags.set(pluginTags)
        }
    }
}

dependencies {
    implementation(project(":code"))?.because("`ArtifactMetaPlugin` uses `SourceSetName`.")

    compileOnlyApi(gradleApi())
    compileOnlyApi(Kotlin.GradlePlugin.api)
    compileOnly(gradleKotlinDsl())
    api(project(":jvm-tools"))
    implementation(project(":plugin-base"))?.because("We need `JavaTaskName` API.")

    testImplementation(project(":plugin-testlib"))
}
