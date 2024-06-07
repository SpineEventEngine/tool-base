/*
 * Copyright 2024, TeamDev. All rights reserved.
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

@file:JvmName("Projects")

package io.spine.tools.gradle.project

import io.spine.tools.code.SourceSetName
import io.spine.tools.code.SourceSetName.Companion.main
import io.spine.tools.code.SourceSetName.Companion.test
import io.spine.tools.gradle.Artifact
import io.spine.tools.gradle.ConfigurationName
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

/**
 * Obtains the Java plugin extension of the project.
 */
public val Project.javaPluginExtension: JavaPluginExtension
    get() = extensions.getByType(JavaPluginExtension::class.java)

/**
 * Obtains source set container of the Java project.
 *
 * Depending on Gradle version, the result is returned
 * either via [JavaPluginExtension] (available since Gradle 7.1),
 * or through [JavaPluginConvention][org.gradle.api.plugins.JavaPluginConvention]
 * (available pre-7.1, now deprecated).
 * This is required to allow ProtoData to be applied with older Gradle versions,
 * such as 6.9.x, actual for Spine 1.x.
 */
@Suppress("DEPRECATION" /* Gradle API for lower Gradle versions. */)
public val Project.sourceSets: SourceSetContainer
    get() {
        return try {
            // Prior to Gradle 7.1 this line will throw `NoSuchMethodError`.
            javaPluginExtension.sourceSets
        } catch (ignored: NoSuchMethodError) {
            val convention = convention.getByType(
                org.gradle.api.plugins.JavaPluginConvention::class.java
            )
            convention.sourceSets
        }
    }

/**
 * Obtains names of the source sets of this project.
 */
public val Project.sourceSetNames: List<SourceSetName>
    get() = sourceSets.map { s -> SourceSetName(s.name) }

/** Obtains a source set by the given name. */
public fun Project.sourceSet(name: String): SourceSet = sourceSets.getByName(name)

/** Obtains a source set by the given name. */
public fun Project.sourceSet(name: SourceSetName): SourceSet = sourceSets.getByName(name.value)

private fun Project.toArtifactBuilder(): Artifact.Builder =
    Artifact.newBuilder().apply {
        group = project.group.toString()
        name = project.name
        version = project.version.toString()
    }

/**
 * Obtains the production [Artifact] of this project.
 */
public val Project.artifact: Artifact
    get() = toArtifactBuilder().build()

/**
 * Obtains the test [Artifact] of this project.
 */
public val Project.testArtifact: Artifact
    get() = toArtifactBuilder().useTestClassifier().build()

/**
 * Obtains the [Artifact] for the given source set.
 *
 * For the `main` source set, the call is equivalent to obtaining [Project.artifact].
 * For the `test` source set, the [Project.testArtifact] will be returned.
 *
 * For other source sets, the given name would be used as a classifier of the artifact.
 */
public fun Project.artifact(ssn: SourceSetName): Artifact {
    return when (ssn) {
        main -> artifact
        test -> testArtifact
        else -> toArtifactBuilder().setClassifier(ssn.value).build()
    }
}

/** Obtains a configuration by its name. */
public fun Project.configuration(name: String): Configuration = configurations.getByName(name)

/** Obtains a configuration by its name. */
public fun Project.configuration(name: ConfigurationName): Configuration =
    configuration(name.value())
