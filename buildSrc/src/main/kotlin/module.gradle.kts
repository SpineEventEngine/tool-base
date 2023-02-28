/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import Module_gradle.Subproject
import io.spine.internal.dependency.CheckerFramework
import io.spine.internal.dependency.Coroutines
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.FindBugs
import io.spine.internal.dependency.Grpc
import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.Kotlin
import io.spine.internal.dependency.Spine
import io.spine.internal.dependency.Truth
import io.spine.internal.gradle.VersionWriter
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.github.pages.updateGitHubPages
import io.spine.internal.gradle.javac.configureErrorProne
import io.spine.internal.gradle.javac.configureJavac
import io.spine.internal.gradle.javadoc.JavadocConfig
import io.spine.internal.gradle.kotlin.applyJvmToolchain
import io.spine.internal.gradle.kotlin.setFreeCompilerArgs
import io.spine.internal.gradle.publish.IncrementGuard
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.testing.configureLogging
import io.spine.internal.gradle.testing.registerTestTasks
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.`java-library`
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    java
    `java-library`
    kotlin("jvm")
    id("net.ltgt.errorprone")
    jacoco
    id("pmd-settings")
    id("write-manifest")
    `project-report`
    idea
}
apply<IncrementGuard>()
apply<VersionWriter>()

CheckStyleConfig.applyTo(project)
JavadocConfig.applyTo(project)
LicenseReporter.generateReportIn(project)

project.run {
    addDependencies()
    forceConfigurations()

    val javaVersion = JavaLanguageVersion.of(11)
    configureJava(javaVersion)
    configureKotlin(javaVersion)

    configureTests()

    configureGitHubPages()
    configureTaskDependencies()
}


typealias Subproject = Project

fun Subproject.addDependencies() {
    dependencies {
        errorprone(ErrorProne.core)

        compileOnlyApi(FindBugs.annotations)
        compileOnlyApi(CheckerFramework.annotations)
        ErrorProne.annotations.forEach { compileOnlyApi(it) }

        implementation(Guava.lib)

        testImplementation(Guava.testLib)
        JUnit.api.forEach { testImplementation(it) }
        Truth.libs.forEach { testImplementation(it) }
        testRuntimeOnly(JUnit.runner)
    }
}

fun Subproject.forceConfigurations() {
    with(configurations) {
        forceVersions()
        excludeProtobufLite()
        all {
            resolutionStrategy {
                val spine = Spine(project)
                @Suppress("DEPRECATION") /* Still need to force `Kotlin.stdLibJdk8` until full
                  migration to Kotlin 1.8.0. */
                force(
                    JUnit.runner,
                    spine.base,
                    spine.validation.runtime,
                    Grpc.stub,
                    Coroutines.jdk8,
                    Coroutines.core,
                    Coroutines.bom,
                    Coroutines.coreJvm,
                    Kotlin.stdLibJdk8
                )
            }
        }

    }
}

fun Subproject.configureJava(javaVersion: JavaLanguageVersion) {
    java {
        toolchain.languageVersion.set(javaVersion)
    }
    tasks {
        withType<JavaCompile>().configureEach {
            configureJavac()
            configureErrorProne()
        }
    }
}

fun Subproject.configureKotlin(javaVersion: JavaLanguageVersion) {
    kotlin {
        applyJvmToolchain(javaVersion.asInt())
        explicitApi()
    }

    tasks {
        withType<KotlinCompile>().configureEach {
            kotlinOptions.jvmTarget = javaVersion.toString()
            setFreeCompilerArgs()
        }
    }
}

fun Subproject.configureTests() {
    tasks {
        registerTestTasks()
        test.configure {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
            configureLogging()
        }
    }
}

fun Subproject.configureGitHubPages() {
    updateGitHubPages(Spine.DefaultVersion.javadocTools) {
        allowInternalJavadoc.set(true)
        rootFolder.set(rootDir)
    }
}


