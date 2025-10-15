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

@file:Suppress("RemoveRedundantQualifierName") // To prevent IDEA replacing FQN imports.

import io.spine.dependency.build.Dokka
import io.spine.gradle.publish.PublishingRepos
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.repo.standardToSpineSdk
import io.spine.gradle.report.coverage.JacocoConfig
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.report.pom.PomGenerator

buildscript {
    standardSpineSdkRepositories()
    doForceVersions(configurations)
    configurations {
        all {
            resolutionStrategy {
                @Suppress("DEPRECATION")
                force(
                    io.spine.dependency.local.Base.lib,
                    io.spine.dependency.local.Reflect.lib,
                    io.spine.dependency.local.Validation.java,
                    io.spine.dependency.lib.Protobuf.GradlePlugin.lib,
                )
            }
        }
    }
    dependencies {
        classpath(io.spine.dependency.local.ToolBase.jvmToolPlugins)
            ?.because("We need `artifactMeta` in `protobuf-tool-plugins`.")
    }
}

plugins {
    id("org.jetbrains.dokka")
    idea
    jacoco
    `project-report`
    `gradle-doctor`
}
JacocoConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.mergeAllReports(project)

spinePublishing {
    val customPublishing = arrayOf(
        "gradle-root-plugin",
        "jvm-tool-plugins",
        "protobuf-tool-plugins",
        "intellij-platform",
        "intellij-platform-java"
    )
    modules = productionModuleNames.toSet()
        .minus(customPublishing)

    modulesWithCustomPublishing = customPublishing.toSet()

    destinations = with(PublishingRepos) {
        setOf(
            cloudArtifactRegistry,
            gitHub("tool-base")
        )
    }
    artifactPrefix = ""
}

allprojects {
    apply(plugin = Dokka.GradlePlugin.id)
    apply(from = "$rootDir/version.gradle.kts")
    group = "io.spine.tools"
    version = extra["versionToPublish"]!!

    repositories.standardToSpineSdk()
}

dependencies {
    productionModules.forEach {
        dokka(it)
    }
}
