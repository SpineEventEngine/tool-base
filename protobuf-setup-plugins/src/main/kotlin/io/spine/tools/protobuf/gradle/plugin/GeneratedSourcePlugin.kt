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
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.plugins.ide.idea.GenerateIdeaModule
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * A Gradle project plugin that configures the Protobuf compilation process to
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
        declareGeneratedDirOutput()
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
 * Obtains `$projectDir/generated/<sourceSet>[/<language>].
 */
context(context: GeneratedDirectoryContext)
private fun GenerateProtoTask.generatedDir(language: String = ""): File {
    val dir = context.generatedDir(project, sourceSet, language)
    return dir.toFile()
}

/**
 * The name of the [GenerateProtoTask] output property for the directory
 * receiving the copies of the generated files.
 */
private const val GENERATED_DIR_PROPERTY = "spineGeneratedSourcesDir"

/**
 * Declares `$projectDir/generated/<sourceSet>` as an output of this task.
 *
 * The files are copied into the directory by [copyGeneratedFiles] as a side effect
 * of the task. Unless the directory is declared as an output, the build cache does
 * not store it, and a task restored from the cache leaves the directory missing,
 * which fails the compilation tasks consuming the copied sources.
 */
context(_: GeneratedDirectoryContext)
private fun GenerateProtoTask.declareGeneratedDirOutput() {
    outputs.dir(generatedDir())
        .withPropertyName(GENERATED_DIR_PROPERTY)
}

/**
 * Copies files from the Protobuf plugin's output base directory into
 * our `$projectDir/generated` directory.
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
 *
 * The replacement directories are added with the dependency on this task so that
 * the tasks consuming the source sets (e.g., compilation, `sourcesJar`) run after
 * the generated code is copied. The dependency carried by the directories excluded
 * by this function is severed, so it must be re-established this way.
 *
 * Generated Kotlin sources are registered through the Kotlin Gradle plugin's dedicated
 * `generatedKotlin` source directory set rather than the plain `kotlin` one, so that
 * build tooling and IDEs can tell them apart from the hand-written code.
 */
@Internal
@OptIn(ExperimentalKotlinGradlePluginApi::class)
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

    /** Obtains the generated directory for the [language] built by this task. */
    fun generatedSrc(language: String): ConfigurableFileCollection =
        project.files(generatedDir(language)).builtBy(this@configureSourceSetDirs)

    val sourceSet = sourceSet

    if (project.hasJava()) {
        val java = sourceSet.java
        excludeFor(java)

        java.srcDir(generatedSrc(JAVA))

        // Add the `grpc` directory unconditionally.
        // We may not have all the `protoc` plugins configured for the task at this time.
        // So, we cannot check if the `grpc` plugin is enabled.
        // It is safe to add the directory anyway because `srcDir()` does not require
        // the directory to exist.
        java.srcDir(generatedSrc(GRPC))
    }

    fun KotlinSourceSet.setup() {
        excludeFor(kotlin)
        generatedKotlin.srcDir(generatedSrc(KOTLIN))
    }

    if (project.hasKotlin()) {
        fun configureKotlin() {
            project.findKotlinSourceSet(sourceSet.name)?.setup()
        }
        // The Kotlin plugin registers the `kotlin` source directory set as a
        // source-set extension once it wires the source set. Gate on its presence
        // to keep the original timing relative to the Protobuf plugin, then drive
        // both the exclusion and the `generatedKotlin` registration from the matching
        // `KotlinSourceSet`, whose `kotlin` set is that same extension.
        if (sourceSet.findKotlinDirectorySet() != null) {
            configureKotlin()
        } else {
            project.afterEvaluate { configureKotlin() }
        }
    }
}

/**
 * Obtains the [KotlinSourceSet] with the given [name], or `null` if the project has no
 * Kotlin extension, or it does not contain a source set with such a name.
 */
private fun Project.findKotlinSourceSet(name: String): KotlinSourceSet? =
    extensions.findByType(KotlinBaseExtension::class.java)
        ?.sourceSets
        ?.findByName(name)

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
 * Ensures the generated dirs exist before an IDEA module is created,
 * so they can be added as sources.
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
