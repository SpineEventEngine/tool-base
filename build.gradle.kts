/*
 * Copyright 2020, TeamDev. All rights reserved.
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

@file:Suppress("RemoveRedundantQualifierName") // To prevent IDEA replacing FQN imports.

import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import io.spine.internal.dependency.CheckerFramework
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.FindBugs
import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.Truth
import io.spine.internal.gradle.IncrementGuard
import io.spine.internal.gradle.Scripts
import io.spine.internal.gradle.VersionWriter
import io.spine.internal.gradle.applyGitHubPackages
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.github.pages.updateGitHubPages
import io.spine.internal.gradle.javac.configureErrorProne
import io.spine.internal.gradle.javac.configureJavac
import io.spine.internal.gradle.javadoc.JavadocConfig
import io.spine.internal.gradle.publish.Publish.Companion.publishProtoArtifact
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.publish.spinePublishing
import io.spine.internal.gradle.report.coverage.JacocoConfig
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.report.pom.PomGenerator
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    jacoco
    idea
    pmd
    `project-report`
    io.spine.internal.dependency.Protobuf.GradlePlugin.apply {
        id(id).version(version)
    }
    io.spine.internal.dependency.ErrorProne.GradlePlugin.apply {
        id(id)
    }
    kotlin("jvm") version io.spine.internal.dependency.Kotlin.version
}

spinePublishing {
    with(PublishingRepos) {
        targetRepositories.addAll(
            cloudRepo,
            cloudArtifactRegistry,
            gitHub("tool-base")
        )
    }
    projectsToPublish.addAll(subprojects.map { it.path })
    spinePrefix.set(true)
}

allprojects {
    apply {
        plugin("jacoco")
        plugin("idea")
        plugin("project-report")
        from("$rootDir/version.gradle.kts")
    }

    group = "io.spine.tools"
    version = extra["versionToPublish"]!!

    with(repositories) {
        applyGitHubPackages("base", project)
        applyStandard()
    }
}

subprojects {
    apply {
        plugin("java-library")
        plugin("kotlin")
        plugin("net.ltgt.errorprone")
        plugin("pmd-settings")
        plugin(Protobuf.GradlePlugin.id)

        with(Scripts) {
            from(testOutput(project))
            from(testArtifacts(project))
            from(slowTests(project))
        }
    }

    dependencies {
        errorprone(ErrorProne.core)
        errorproneJavac(ErrorProne.javacPlugin)

        compileOnlyApi(FindBugs.annotations)
        compileOnlyApi(CheckerFramework.annotations)
        ErrorProne.annotations.forEach { compileOnlyApi(it) }

        implementation(Guava.lib)

        testImplementation(Guava.testLib)
        JUnit.api.forEach { testImplementation(it) }
        Truth.libs.forEach { testImplementation(it) }
        testRuntimeOnly(JUnit.runner)
    }

    with(configurations) {
        forceVersions()
        excludeProtobufLite()
    }

    val javaVersion = JavaVersion.VERSION_1_8

    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    tasks.withType<JavaCompile> {
        configureJavac()
        configureErrorProne()
    }

    CheckStyleConfig.applyTo(project)
    JavadocConfig.applyTo(project)
    LicenseReporter.generateReportIn(project)

    kotlin {
        explicitApi()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            freeCompilerArgs = listOf(
                "-Xskip-prerelease-check",
                "-Xjvm-default=all",
                "-Xopt-in=kotlin.contracts.ExperimentalContracts"
            )
        }
    }

    tasks.test {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
    }

    val generatedDir = "$projectDir/generated"

    protobuf {
        generatedFilesBaseDir = generatedDir
        protoc {
            artifact = Protobuf.compiler
        }
    }

    tasks.clean {
        delete(generatedDir)
    }

    apply<IncrementGuard>()
    apply<VersionWriter>()

    publishProtoArtifact(project)

    val spineBaseVersion: String by extra
    updateGitHubPages(spineBaseVersion) {
        allowInternalJavadoc.set(true)
        rootFolder.set(rootDir)
    }
}

JacocoConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.mergeAllReports(project)
