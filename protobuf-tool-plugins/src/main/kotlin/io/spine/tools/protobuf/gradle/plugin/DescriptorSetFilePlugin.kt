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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING

/**
 * A Gradle project plugin that configures Protobuf generation tasks to produce
 * descriptor set files and expose them as resources of the corresponding source set.
 *
 * This plugin reproduces the behavior of `GenerateProtoTask.setupDescriptorSetFileCreation()`
 * defined in this repository's buildSrc utilities, but exposes it as a reusable plugin.
 */
public class DescriptorSetFilePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Defer configuration until after evaluation to ensure all tasks are present.
        project.afterEvaluate {
            val tasks = project.tasks
            tasks.matching { it.isGenerateProtoTask() }.configureEach { task ->
                configureGenerateProtoTask(project, task)
            }
        }
    }

    private fun Task.isGenerateProtoTask(): Boolean =
        try {
            val type = this.javaClass
            type.getMethod("getSourceSet")
            type.getMethod("getDescriptorSetOptions")
            type.getMethod("setGenerateDescriptorSet", java.lang.Boolean.TYPE)
            true
        } catch (_: NoSuchMethodException) {
            false
        }

    private fun configureGenerateProtoTask(project: Project, task: Task) {
        // Enable descriptor set generation via reflection.
        task.callSetter("setGenerateDescriptorSet", true)

        // Obtain SourceSet from the task via reflection.
        val sourceSet = task.callGetter("getSourceSet") as SourceSet
        val ssName = sourceSet.name
        val buildDir = project.layout.buildDirectory.asFile.get().path
        val descriptorsDir = "$buildDir/descriptors/$ssName"
        val descriptorName = project.descriptorSetName(sourceSet)

        // Configure descriptor set options via reflection.
        val options = task.callGetter("getDescriptorSetOptions")
        options!!.callSetter("setPath", "$descriptorsDir/$descriptorName")
        options.callSetter("setIncludeImports", true)
        options.callSetter("setIncludeSourceInfo", true)

        // Add the descriptors directory to the resources of the corresponding source set.
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val ssObj = sourceSets.getByName(ssName)
        ssObj.resources.srcDir(descriptorsDir)

        // Create a `desc.ref` file pointing to the descriptor file name once the task finishes.
        task.doLast {
            val descRefFile = File(descriptorsDir, "desc.ref")
            descRefFile.parentFile.mkdirs()
            descRefFile.createNewFile()
            try {
                Files.write(descRefFile.toPath(), setOf(descriptorName), TRUNCATE_EXISTING)
            } catch (e: Exception) {
                project.logger.error("Error writing `${descRefFile.absolutePath}`.", e)
                throw e
            }
        }
    }
}

private fun Any.callGetter(name: String): Any? =
    this.javaClass.getMethod(name).invoke(this)

private fun Any.callSetter(name: String, value: Any) {
    val paramType = when (value) {
        is Boolean -> java.lang.Boolean.TYPE
        is Int -> java.lang.Integer.TYPE
        else -> value.javaClass
    }
    this.javaClass.getMethod(name, paramType).invoke(this, value)
}

private fun Project.descriptorSetName(sourceSet: SourceSet): String =
    arrayOf(
        group.toString(),
        name,
        sourceSet.name,
        version.toString()
    ).joinToString(separator = "_", postfix = ".desc")
