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

import io.spine.dependency.build.CheckerFramework
import io.spine.dependency.build.ErrorProne
import io.spine.dependency.build.FindBugs
import io.spine.dependency.kotlinx.Coroutines
import io.spine.dependency.lib.Grpc
import io.spine.dependency.lib.GrpcKotlin
import io.spine.dependency.lib.Kotlin
import io.spine.dependency.local.ArtifactVersion
import io.spine.dependency.local.Base
import io.spine.dependency.local.Logging
import io.spine.dependency.local.Reflect
import io.spine.dependency.local.Validation
import io.spine.gradle.VersionWriter
import io.spine.gradle.checkstyle.CheckStyleConfig
import io.spine.gradle.github.pages.updateGitHubPages
import io.spine.gradle.javac.configureErrorProne
import io.spine.gradle.javac.configureJavac
import io.spine.gradle.javadoc.JavadocConfig
import io.spine.gradle.kotlin.setFreeCompilerArgs
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.report.license.LicenseReporter
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `java-library`
    `java-test-fixtures`
    kotlin("jvm")
    id("module-testing")
    id("net.ltgt.errorprone")
    id("org.jetbrains.dokka")
    id("detekt-code-analysis")
    id("pmd-settings")
    id("write-manifest")
    jacoco
    `project-report`
    idea
}
apply<IncrementGuard>()
apply<VersionWriter>()

CheckStyleConfig.applyTo(project)
JavadocConfig.applyTo(project)
LicenseReporter.generateReportIn(project)

project.run {
    forceConfigurations()
    addDependencies()

    configureJava()
    configureKotlin()

    configureTests()
    configureDocTasks()

    configureGitHubPages()
    configureTaskDependencies()
}

typealias Module = Project

fun Module.addDependencies() {
    dependencies {
        errorprone(ErrorProne.core)

        compileOnlyApi(FindBugs.annotations)
        compileOnlyApi(CheckerFramework.annotations)
        ErrorProne.annotations.forEach { compileOnlyApi(it) }
    }
}

fun Module.forceConfigurations() {
    configurations.run {
        forceVersions()
        excludeProtobufLite()
        all {
            resolutionStrategy {
                eachDependency {
                    val configuration = this@all
                    if (configuration.name.contains("detekt", ignoreCase = true)) {
                        if (requested.group == Kotlin.group) {
                            useVersion(Kotlin.embeddedVersion)
                            because("Force Kotlin version in Detekt configuration")
                        }
                    } else if (requested.group == Kotlin.group) {
                        useVersion(Kotlin.runtimeVersion)
                    }
                    if (requested.name.contains(Coroutines.infix)) {
                        useVersion(Coroutines.version)
                    }
                }
                force(
                    Base.lib,
                    Reflect.lib,
                    Logging.lib,
                    Logging.libJvm,
                    Validation.runtime,
                    Grpc.stub,
                    GrpcKotlin.ProtocPlugin.artifact,
                )
            }
        }
    }
}

fun Module.configureJava() {
    java {
        toolchain.languageVersion.set(BuildSettings.javaVersion)
    }
    tasks {
        withType<JavaCompile>().configureEach {
            configureJavac()
            configureErrorProne()
        }
    }
}

fun Module.configureKotlin() {
    kotlin {
        explicitApi()
        compilerOptions {
            jvmTarget.set(BuildSettings.jvmTarget)
            setFreeCompilerArgs()
        }
    }
}

fun Module.configureTests() {
    tasks {
        test.configure {
            // See https://github.com/gradle/gradle/issues/18647.
            jvmArgs(
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.util=ALL-UNNAMED"
            )
        }
    }
}

fun Module.configureDocTasks() {
    val dokkaJavadoc by tasks.getting(DokkaTask::class)
    tasks.register("javadocJar", Jar::class) {
        from(dokkaJavadoc.outputDirectory)
        archiveClassifier.set("javadoc")
        dependsOn(dokkaJavadoc)
    }

    afterEvaluate {
        dokka {
            configureForKotlin(
                project,
                DocumentationSettings.SourceLink.url
            )
        }
    }
}

fun Module.configureGitHubPages() {
    updateGitHubPages(ArtifactVersion.javadocTools) {
        allowInternalJavadoc.set(true)
        rootFolder.set(rootDir)
    }
}


