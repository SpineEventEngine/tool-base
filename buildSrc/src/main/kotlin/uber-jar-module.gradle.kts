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

@file:Suppress("UnstableApiUsage") // `configurations` block.

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.publish.SpinePublishing
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.report.license.LicenseReporter

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow")
    id("write-manifest")
}
apply<IncrementGuard>()
LicenseReporter.generateReportIn(project)

spinePublishing {
    artifactPrefix = ""
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

/**
 * Declare dependency explicitly to address the Gradle error.
 */
@Suppress("unused")
val publishFatJarPublicationToMavenLocal: Task by tasks.getting {
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
    archiveClassifier.set("")  /** To prevent Gradle setting something like `osx-x86_64`. */
}
