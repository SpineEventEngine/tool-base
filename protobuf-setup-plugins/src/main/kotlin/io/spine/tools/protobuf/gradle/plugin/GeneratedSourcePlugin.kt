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

package io.spine.tools.protobuf.gradle.plugin

import com.google.protobuf.gradle.GenerateProtoTask
import io.spine.tools.code.SourceSetName
import io.spine.tools.gradle.protobuf.generated
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
import org.gradle.plugins.ide.idea.GenerateIdeaModule
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * A Gradle project plugin that configures Protobuf compilation process to
 * put the resuling output to the `generated` directory under the project root.
 *
 * The plugin does the following:
 *  - enables Kotlin builtin for `protoc`;
 *  - makes the `GenerateProtoTask` copy generated sources into
 *    `$projectDir/generated/<sourceSet>/{java,kotlin}`;
 *  - excludes Protobuf plugin output directories from Kotlin/Java source sets and
 *    includes the copied ones;
 *  - makes Kotlin compilation and `processResources` depend on the corresponding
 *    `GenerateProtoTask` to avoid race conditions;
 *  - ensures generated source directories exist for IDEA module configuration.
 */
public class GeneratedSourcePlugin : ProtobufSetupPlugin() {

    internal companion object {

        /**
         * The ID of this Gradle plugin.
         */
        const val id = "io.spine.generated-source"
    }

    override fun setup(task: GenerateProtoTask): Unit = with(task) {
        builtins.maybeCreate("kotlin")
        excludeProtocOutput()
        doLast {
            copyGeneratedFiles()
        }
        setupKotlinCompile()
        makeDirsForIdeaModule()
    }
}

/**
 * Obtains `$projectDir/generated/<sourceSet>[/<language>]`.
 */
private fun GenerateProtoTask.generatedDir(language: String = ""): File {
    val ssn = SourceSetName(sourceSet.name)
    val base: Path = project.generated(ssn)
    val dir = if (language.isBlank()) base else base.resolve(language)
    dir.createDirectories()
    return dir.toFile()
}

/**
 * Copies files from the Protobuf plugin's output base directory into our `$projectDir/generated`
 * directory and removes `com/google` packages to avoid conflicts with library classes.
 */
private fun GenerateProtoTask.copyGeneratedFiles() {
    project.copy { spec ->
        spec.from(this@copyGeneratedFiles.outputBaseDir)
        spec.into(generatedDir())
    }
}

/**
 * Exclude directories produced under `$buildDir/generated/(source|sources)/proto` from
 * both Java and Kotlin source sets and replace them with `$projectDir/generated/...`.
 */
private fun GenerateProtoTask.excludeProtocOutput() {
    val protocOutputDir = File(outputBaseDir).parentFile

    fun filterFor(directorySet: SourceDirectorySet) {
        val newSourceDirectories = directorySet.sourceDirectories
            .filter { !it.residesIn(protocOutputDir) }
            .toSet()
        directorySet.setSrcDirs(listOf<String>())
        directorySet.srcDirs(newSourceDirectories)
    }

    val java: SourceDirectorySet = sourceSet.java
    filterFor(java)
    java.srcDir(generatedDir("java"))

    val kotlin = sourceSet.kotlinOrNull
    if (kotlin != null) {
        filterFor(kotlin)
        kotlin.srcDir(generatedDir("kotlin"))
    }
}

private val SourceSet.kotlinOrNull: SourceDirectorySet?
    get() = try {
        (this as ExtensionAware).extensions.findByName("kotlin") as SourceDirectorySet?
    } catch (_: Throwable) {
        null
    }

private fun File.residesIn(directory: File): Boolean =
    canonicalFile.startsWith(directory.absolutePath)

/**
 * Ensure Kotlin compilation explicitly depends on this `GenerateProtoTask`.
 */
private fun GenerateProtoTask.setupKotlinCompile() {
    val taskName = sourceSet.getCompileTaskName("Kotlin")
    try {
        val kotlinCompile = project.tasks.named(taskName, KotlinCompilationTask::class.java).orNull
        kotlinCompile?.dependsOn(this)
    } catch (_: Throwable) {
        // Kotlin plugin is likely not applied; nothing to do.
    }
}

/**
 * Ensure the generated dirs exist before IDEA module is created so they can be added as sources.
 */
private fun GenerateProtoTask.makeDirsForIdeaModule() {
    project.plugins.withId("idea") {
        val javaDir = generatedDir("java")
        val kotlinDir = generatedDir("kotlin")
        project.tasks.withType(GenerateIdeaModule::class.java).forEach {
            it.doFirst {
                javaDir.mkdirs()
                kotlinDir.mkdirs()
            }
        }
    }
}
