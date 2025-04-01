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
import io.spine.dependency.build.Dokka
import io.spine.dependency.build.ErrorProne
import io.spine.dependency.build.FindBugs
import io.spine.dependency.lib.Coroutines
import io.spine.dependency.lib.Grpc
import io.spine.dependency.lib.Guava
import io.spine.dependency.lib.Jackson
import io.spine.dependency.lib.Kotlin
import io.spine.dependency.local.ArtifactVersion
import io.spine.dependency.local.Logging
import io.spine.dependency.local.Spine
import io.spine.dependency.local.Validation
import io.spine.dependency.test.JUnit
import io.spine.dependency.test.Kotest
import io.spine.dependency.test.Truth
import io.spine.gradle.VersionWriter
import io.spine.gradle.checkstyle.CheckStyleConfig
import io.spine.gradle.github.pages.updateGitHubPages
import io.spine.gradle.javac.configureErrorProne
import io.spine.gradle.javac.configureJavac
import io.spine.gradle.javadoc.JavadocConfig
import io.spine.gradle.kotlin.setFreeCompilerArgs
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.testing.configureLogging
import io.spine.gradle.testing.registerTestTasks
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    `java-library`
    kotlin("jvm")
    id("net.ltgt.errorprone")
    id("detekt-code-analysis")
    id("pmd-settings")
    id("write-manifest")
    jacoco
    `project-report`
    idea
}
apply<IncrementGuard>()
apply<VersionWriter>()

apply {
    plugin(Dokka.GradlePlugin.id)
}

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

        testImplementation(Guava.testLib)
        testImplementation(Kotest.assertions)
        JUnit.api.forEach { testImplementation(it) }
        Truth.libs.forEach { testImplementation(it) }
        testRuntimeOnly(JUnit.runner)
    }
}

fun Module.forceConfigurations() {
    with(configurations) {
        forceVersions()
        excludeProtobufLite()
        all {
            resolutionStrategy {
                @Suppress("DEPRECATION") // To force `Kotlin.stdLibJdk7` version.
                force(
                    Kotlin.stdLibJdk7,
                    JUnit.runner,
                    Spine.base,
                    Spine.reflect,
                    Logging.lib,
                    Logging.libJvm,
                    Validation.runtime,
                    Grpc.stub,
                    Coroutines.jdk8,
                    Coroutines.core,
                    Coroutines.bom,
                    Coroutines.coreJvm,
                    Jackson.Junior.objects
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
        registerTestTasks()
        test.configure {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
            configureLogging()

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

    tasks.withType<DokkaTaskPartial>().configureEach {
        configureForKotlin()
    }
}

fun Module.configureGitHubPages() {
    updateGitHubPages(ArtifactVersion.javadocTools) {
        allowInternalJavadoc.set(true)
        rootFolder.set(rootDir)
    }
}


