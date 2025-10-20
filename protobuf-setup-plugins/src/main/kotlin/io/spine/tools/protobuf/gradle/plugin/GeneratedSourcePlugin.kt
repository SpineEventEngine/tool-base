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
import io.spine.annotation.Internal
import io.spine.tools.fs.DirectoryName
import io.spine.tools.gradle.project.hasJava
import io.spine.tools.gradle.project.hasKotlin
import io.spine.tools.gradle.task.findKotlinDirectorySet
import io.spine.tools.protobuf.gradle.GeneratedDirectoryContext
import io.spine.tools.protobuf.gradle.plugin.GeneratedSubdir.GRPC
import io.spine.tools.protobuf.gradle.plugin.GeneratedSubdir.JAVA
import io.spine.tools.protobuf.gradle.plugin.GeneratedSubdir.KOTLIN
import io.spine.tools.resolve
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.plugins.ide.idea.GenerateIdeaModule
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * A Gradle project plugin that configures Protobuf compilation process to
 * put the resulting output to the `generated` directory under the project root.
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
public class GeneratedSourcePlugin : ProtobufSetupPlugin(), GeneratedDirectoryContext {

    internal companion object {

        /**
         * The ID of this Gradle plugin.
         */
        const val id = "io.spine.generated-sources"
    }

    override fun setup(task: GenerateProtoTask): Unit = with(task) {
        builtins.maybeCreate("kotlin")
        configureSourceSetDirs()
        doLast {
            copyGeneratedFiles()
        }
        setupKotlinCompile()
        makeDirsForIdeaModule()
    }

    /**
     * Resolves the directory as `$projectDir/generated/<sourceSet>[/<language>]`
     */
    override fun generatedDir(
        project: Project,
        sourceSet: SourceSet,
        language: String
    ): Path {
        val generatedDir = project.projectDir.resolve(DirectoryName.generated).toPath()
        return generatedDir.resolve("${sourceSet.name}/$language")
    }
}

/**
 * Obtains `$projectDir/generated/<sourceSet>[/<language>]` and creates it, if
 * the directory does not exist yet.
 */
context(context: GeneratedDirectoryContext)
private fun GenerateProtoTask.generatedDir(language: String = ""): File {
    val dir = context.generatedDir(project, sourceSet, language)
    dir.createDirectories()
    return dir.toFile()
}

/**
 * Copies files from the Protobuf plugin's output base directory into our `$projectDir/generated`
 * directory and removes `com/google` packages to avoid conflicts with library classes.
 */
context(_: GeneratedDirectoryContext)
private fun GenerateProtoTask.copyGeneratedFiles() {
    project.copy { spec ->
        spec.from(this@copyGeneratedFiles.outputBaseDir)
        spec.into(generatedDir())
    }
}

/**
 * The names of the subdirectories where the Compiler places generated files.
 */
private object GeneratedSubdir {
    const val JAVA = "java"
    const val KOTLIN = "kotlin"
    const val GRPC = "grpc"
}

/**
 * Exclude directories produced under `$buildDir/generated/(source|sources)/proto` from
 * both Java and Kotlin source sets and replace them with `$projectDir/generated/...`.
 */
@Internal
context(_: GeneratedDirectoryContext)
public fun GenerateProtoTask.configureSourceSetDirs() {
    val project = project
    val protocOutputDir = File(outputBaseDir).parentFile

    /** Filters out directories belonging to `build/generated/source/proto`. */
    fun excludeFor(lang: SourceDirectorySet) {
        val newSourceDirectories = lang.sourceDirectories
            .filter { !it.residesIn(protocOutputDir) }
            .toSet()

        // Clear the source directories of the Java source set.
        // This trick was needed when building the `base` module of Spine.
        // Otherwise, the `java` plugin would complain about duplicate source files.
        lang.setSrcDirs(listOf<String>())

        // Add the filtered directories back to the Java source set.
        lang.srcDirs(newSourceDirectories)
    }

    val sourceSet = sourceSet

    if (project.hasJava()) {
        val java = sourceSet.java
        excludeFor(java)

        java.srcDir(generatedDir(JAVA))

        // Add the `grpc` directory unconditionally.
        // We may not have all the `protoc` plugins configured for the task at this time.
        // So, we cannot check if the `grpc` plugin is enabled.
        // It is safe to add the directory anyway, because `srcDir()` does not require
        // the directory to exist.
        java.srcDir(generatedDir(GRPC))
    }

    fun SourceDirectorySet.setup() {
        excludeFor(this@setup)
        srcDirs(generatedDir(KOTLIN))
    }

    if (project.hasKotlin()) {
        val kotlinDirectorySet = sourceSet.findKotlinDirectorySet()
        kotlinDirectorySet?.setup()
            ?: project.afterEvaluate {
                sourceSet.findKotlinDirectorySet()?.setup()
            }
    }
}

private fun File.residesIn(directory: File): Boolean =
    canonicalFile.startsWith(directory.absolutePath)

/**
 * Ensure Kotlin compilation explicitly depends on this `GenerateProtoTask`.
 */
@Internal
public fun GenerateProtoTask.setupKotlinCompile() {
    val taskName = sourceSet.getCompileTaskName("Kotlin")
    try {
        val kotlinCompile = project.tasks.named(taskName, KotlinCompilationTask::class.java).orNull
        kotlinCompile?.dependsOn(this)
    } catch (_: Throwable) {
        // Kotlin plugin is likely not applied; nothing to do.
    }
}

/**
 * Ensures the generated dirs exist before IDEA module is created, so they can be added as sources.
 */
@Internal
context(_: GeneratedDirectoryContext)
public fun GenerateProtoTask.makeDirsForIdeaModule() {
    project.plugins.withId("idea") {
        val javaDir = generatedDir(JAVA)
        val kotlinDir = generatedDir(KOTLIN)
        project.tasks.withType(GenerateIdeaModule::class.java).forEach {
            it.doFirst {
                javaDir.mkdirs()
                kotlinDir.mkdirs()
            }
        }
    }
}
