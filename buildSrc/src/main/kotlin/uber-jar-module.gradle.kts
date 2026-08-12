/*
 * Copyright 2026, TeamDev. All rights reserved.
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

@file:Suppress("UnstableApiUsage") // `configurations` block.

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.spine.dependency.lib.Caffeine
import io.spine.dependency.lib.Jackson
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.publish.SpinePublishing
import io.spine.gradle.publish.setup
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.report.license.LicenseReporter

plugins {
    id("module")
    `maven-publish`
    id("com.gradleup.shadow")
    id("write-manifest")
    `project-report`
    idea
}
apply<IncrementGuard>()
LicenseReporter.generateReportIn(project)

/*
 * Align third-party components shared with the rest of the Spine stack
 * to the versions this repository is built and tested with.
 *
 * The versions resolved here become the pins of the published POM entries
 * (see `declareUnshadedDependencies`). Without this block, the versions
 * requested by the IntelliJ Platform POMs stand — e.g. Jackson `2.13.0`,
 * Caffeine `3.0.4` — and every consumer resolving with
 * `failOnVersionConflict()` fails on the disagreement with the versions
 * the rest of its graph requests. Guava and other components covered by
 * `forceVersions()` of the `module` plugin are aligned the same way already.
 */
configurations.all {
    resolutionStrategy {
        Jackson.forceArtifacts(project, this@all, this@resolutionStrategy)
        Jackson.Junior.forceArtifacts(project, this@all, this@resolutionStrategy)
        force(
            Jackson.annotations,
            Caffeine.lib,
        )
    }
}

spinePublishing {
    // This prefix does not apply to the modules of this project because they all belong
    // to the `io.spine.tools` group, and therefore `toolArtifactPrefix` applies instead.
    artifactPrefix = ""
    toolArtifactPrefix = "NONE"
    destinations = rootProject.the<SpinePublishing>().destinations
    customPublishing = true
}

/** The ID of the far JAR artifact. */
private val projectArtifact = project.name.replace(":", "")

publishing {
    val groupName = project.group.toString()
    val versionName = project.version.toString()

    publications {
        create("fatJar", MavenPublication::class) {
            groupId = groupName
            artifactId = projectArtifact
            version = versionName
            artifact(tasks.shadowJar)
        }
    }
}

// Declare dependency explicitly to address the Gradle error.
tasks.getByName("publishFatJarPublicationToMavenLocal") {
    dependsOn(tasks.shadowJar)
}

// Disable the `jar` task to free up the name of the resulting archive.
tasks.jar {
    enabled = false
}

tasks.publish {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    setup()
    excludeFiles()
    isZip64 = true  /* The archive has way too many items. So using the Zip64 mode. */
    archiveClassifier.set("")  /** To prevent Gradle setting something like `osx-x86_64`. */
}

/**
 * Exclude unwanted directories.
 */
@Suppress("LongMethod")
private fun ShadowJar.excludeFiles() {
    exclude(
        /*
          Exclude IntelliJ Platform images and other resources associated with IntelliJ UI.
          We do not call the UI, so they won't be used.
         */
        "actions/**",
        "chooser/**",
        "codeStyle/**",
        "codeStylePreview/**",
        "codeWithMe/**",
        "darcula/**",
        "debugger/**",
        "diff/**",
        "duplicates/**",
        "expui/**",
        "extensions/**",
        "fileTemplates/**",
        "fileTypes/**",
        "general/**",
        "graph/**",
        "gutter/**",
        "hierarchy/**",
        "icons/**",
        "ide/**",
        "idea/**",
        "inlayProviders/**",
        "inspectionDescriptions/**",
        "inspectionReport/**",
        "intentionDescriptions/**",
        "javadoc/**",
        "javaee/**",
        "json/**",
        "liveTemplates/**",
        "mac/**",
        "modules/**",
        "nodes/**",
        "objectBrowser/**",
        "plugins/**",
        "postfixTemplates/**",
        "preferences/**",
        "process/**",
        "providers/**",
        "runConfigurations/**",
        "scope/**",
        "search/**",
        "toolbar/**",
        "toolbarDecorator/**",
        "toolwindows/**",
        "vcs/**",
        "webreferences/**",
        "welcome/**",
        "windows/**",
        "xml/**",

        /* Exclude IDEA project templates. */
        "resources/projectTemplates/**",

        /*
          Exclude dynamic libraries. Should the tool users need them,
          they would add them explicitly.
         */
        "bin/**",

        /*
          Exclude Google Protobuf definitions to avoid duplicates.
         */
        "google/**",
        "src/google/**",

        /**
         * Exclude Spine Protobuf definitions to avoid duplications.
         */
        "spine/**",

        /**
         * Exclude Kotlin runtime because it will be provided.
         */
        "kotlin/**",
        "kotlinx/**",

        /**
         * Exclude native libraries related to debugging.
         */
        "win32-x86/**",
        "win32-x86-64/**",

        /*
          Exclude the JetBrains fork of JNA (`org.jetbrains.intellij.deps.jna`),
          which arrives transitively with the IntelliJ Platform artifacts.
          Despite the `com.sun.jna` package, JNA is not part of the JDK, and
          the unrelocated classes would shadow the genuine `net.java.dev.jna`
          artifacts on a consumer's classpath. The headless PSI code does not
          use this OS-integration layer. Should the tool users need JNA, they
          would add `net.java.dev.jna:jna:5.9.0` (a drop-in) explicitly.

          This entry must stay in the shared list: `intellij-platform-java`
          excludes whatever the `intellij-platform` JAR already contains, so
          an exclusion made only in one module resurfaces the files in the
          other module's JAR.
         */
        "com/sun/jna/**",
    )
}
