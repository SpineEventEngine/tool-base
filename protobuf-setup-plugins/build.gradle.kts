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
import io.spine.dependency.lib.Protobuf
import io.spine.dependency.local.Base
import io.spine.dependency.local.Logging
import io.spine.gradle.isSnapshot
import io.spine.gradle.publish.SpinePublishing
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.shade.unbundleProtobufRuntime
import io.spine.gradle.testing.enableTestKitCoverage
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata

plugins {
    `uber-jar-module`
    `plugin-publish`
    id("io.spine.artifact-meta")
}
LicenseReporter.generateReportIn(project)

// Credit coverage of the plugins executed in TestKit worker JVMs by the
// `*PluginSpec` tests. See `io.spine.gradle.testing.enableTestKitCoverage`.
enableTestKitCoverage()

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
val versionToPublish = extra["versionToPublish"] as String

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
val publishPlugins = tasks.getByName("publishPlugins") {
    enabled = !versionToPublish.isSnapshot()
}

tasks.getByName("publish") {
    dependsOn(publishPlugins)
}

dependencies {
    compileOnlyApi(gradleApi())
    compileOnly(gradleKotlinDsl())

    // Access to `GenerateProtoTask` and related APIs at compile and runtime.
    implementation(Protobuf.GradlePlugin.lib)

    implementation(Base.lib)?.because("We need `DescriptorSetReferenceFile` at compile time.")
    implementation(project(":code"))?.because("We need `SourceSetName`.")
    implementation(project(":fs"))?.because("We need `DescriptorsDir` and `DirectoryName`.")
    implementation(project(":java-code"))?.because("We need `DefaultJavaPaths`.")
    implementation(project(":proto-code"))?.because(
        "We need `FileDescriptorSuperset`, `MoreKnownTypes`, and the proto `Directory`."
    )
    implementation(project(":plugin-base"))
    implementation(Logging.lib)

    testImplementation(project(":plugin-testlib"))
}

// Put the Kotlin Gradle plugin on the plugin-under-test classpath so the TestKit
// spec that applies it shares a classloader with this plugin, which uses the Kotlin
// Gradle plugin API (`KotlinSourceSet.generatedKotlin`).
val kotlinGradlePlugin = configurations.detachedConfiguration(
    dependencies.create(Kotlin.GradlePlugin.lib)
)
tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(kotlinGradlePlugin)
}

publishing.publications.withType<MavenPublication>().configureEach {
    when (name) {
        "fatJar" -> {
            // Avoid the conflict with the `pluginMaven` publication.
            artifactId = "protobuf-setup-plugins-all"
        }
    }
}

// Declare the Protobuf runtime as a dependency rather than bundle it,
// so that the runtime resolved by the consumer wins.
unbundleProtobufRuntime()
