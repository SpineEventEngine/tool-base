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

@file:JvmName("Projects")

package io.spine.tools.gradle.project

import com.google.common.collect.ImmutableList
import io.spine.tools.code.Java
import io.spine.tools.code.Kotlin
import io.spine.tools.code.Language
import io.spine.tools.code.SourceSetName
import io.spine.tools.code.SourceSetName.Companion.main
import io.spine.tools.gradle.ConfigurationName
import io.spine.tools.meta.MavenArtifact
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * Tells if this project can deal with Java code.
 *
 * @return `true` if `java` plugin is installed, `false` otherwise.
 */
public fun Project.hasJava(): Boolean = pluginManager.hasPlugin(Java.name.lowercase())

/**
 * Obtains the Java plugin extension of the project.
 */
public val Project.javaPluginExtension: JavaPluginExtension
    get() = extensions.getByType(JavaPluginExtension::class.java)

/**
 * Obtains source set container of the Java project.
 */
public val Project.sourceSets: SourceSetContainer
    get() {
        return javaPluginExtension.sourceSets
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

/**
 * Obtains an artifact for the given source set.
 *
 * For the `main` source set, the call is equivalent to obtaining a [MavenArtifact] with
 * the `group`, `name`, and `version` properties of the project.
 *
 * For other source sets, the given source set name would be used as a classifier of the artifact.
 */
public fun Project.artifact(ssn: SourceSetName): MavenArtifact {
    //TODO:2025-10-15:alexander.yevsyukov: compose `artifact` taking into account `MavenPublication`
    val classifier = if (ssn == main) null else ssn.value
    return MavenArtifact(group.toString(), name, version.toString(), classifier)
}

/** Obtains a configuration by its name. */
public fun Project.configuration(name: String): Configuration = configurations.getByName(name)

/** Obtains a configuration by its name. */
public fun Project.configuration(name: ConfigurationName): Configuration =
    configuration(name.value())

/**
 * Tells if this project can deal with Kotlin code.
 *
 * @return `true` if any of the tasks starts with `"compile"` and ends with `"Kotlin"`.
 */
public fun Project.hasKotlin(): Boolean = hasCompileTask(Kotlin)

/**
 * Tells if this project has a compile task for the given language.
 */
public fun Project.hasCompileTask(language: Language): Boolean {
    val currentTasks = ImmutableList.copyOf(tasks)
    val compileTask = currentTasks.find {
        it.name.startsWith("compile") && it.name.endsWith(language.name)
    }
    return compileTask != null
}

/**
 * Verifies if the project can deal with Java or Kotlin code.
 *
 * The current Protobuf support of Kotlin is based on Java codegen.
 * Therefore, it is likely that Java would be enabled in the project for
 * Kotlin proto code to be generated.
 * Though, it may change someday, and Kotlin support for Protobuf would be
 * self-sufficient. This method assumes such a case when it checks the presence of
 * Kotlin compilation tasks.
 *
 * @see [hasJava]
 * @see [hasKotlin]
 */
public fun Project.hasJavaOrKotlin(): Boolean = hasJava() || hasKotlin()

/**
 * Attempts to obtain the [Java compilationGradle task][SourceSet.getCompileJavaTaskName]
 * for the given source set.
 */
public fun Project.findJavaCompileFor(sourceSet: SourceSet): JavaCompile? {
    val taskName = sourceSet.compileJavaTaskName
    return tasks.findByName(taskName) as JavaCompile?
}

/**
 * Attempts to obtain the Kotlin compilation Gradle task for the given source set.
 *
 * Typically, the task is named by a pattern: `compile<SourceSet name>Kotlin`, or just
 * `compileKotlin` if the source set name is `"main"`.
 * If the task does not fit this described pattern, this method will not find it.
 */
public fun Project.findKotlinCompileFor(sourceSet: SourceSet): KotlinCompilationTask<*>? {
    val taskName = sourceSet.getCompileTaskName(Kotlin.name)
    return tasks.findByName(taskName) as KotlinCompilationTask<*>?
}
