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
import io.spine.code.proto.DescriptorSetReferenceFile
import io.spine.tools.code.SourceSetName
import io.spine.tools.gradle.task.JavaTaskName
import io.spine.tools.protobuf.gradle.descriptorSetFile
import java.io.File
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

/**
 * A Gradle project plugin that configures Protobuf generation tasks to produce
 * descriptor set files and expose them as resources of the corresponding source set.
 */
public class DescriptorSetFilePlugin : ProtobufSetupPlugin() {

    internal companion object {

        /**
         * The ID of this Gradle plugin.
         */
        const val id = "io.spine.descriptor-set-file"

        /**
         * The name of the [GenerateProtoTask] input property holding the project version.
         */
        const val VERSION_PROPERTY = "projectVersion"
    }

    override fun setup(task: GenerateProtoTask) {
        val project = task.project
        val sourceSet = task.sourceSet

        // Enable descriptor set generation.
        task.generateDescriptorSet = true

        val ssn = SourceSetName(sourceSet.name)
        val descriptorSetFile = project.descriptorSetFile(ssn)
        val descriptorsDir = descriptorSetFile.parentFile

        // Configure descriptor set options.
        with(task.descriptorSetOptions) {
            path = descriptorSetFile.absolutePath
            includeImports = true
            includeSourceInfo = true
        }

        // Add the `descriptors` directory to the resources so that
        // the descriptor set file, and the reference file which is created in
        // the `doLast` block below are packed together with the class files.
        sourceSet.resources.srcDir(descriptorsDir.absolutePath)

        // Create a `desc.ref` file pointing to the descriptor file name once the task finishes.
        task.doLast {
            DescriptorSetReferenceFile.create(descriptorsDir, descriptorSetFile)
        }
        task.declareReferenceFileOutput(descriptorsDir)
        task.declareVersionInput()

        task.dependOnProcessResourcesTask()
    }
}

/**
 * The name of the [GenerateProtoTask] output property for the descriptor set reference file.
 */
private const val REFERENCE_FILE_PROPERTY = "spineDescriptorSetReferenceFile"

/**
 * Declares the [reference file][DescriptorSetReferenceFile] written in
 * the `doLast` action of this task as a task output.
 *
 * The descriptor set file itself is a declared output of [GenerateProtoTask],
 * but the reference file is created as a side effect of the task.
 * Unless it is declared as an output, the build cache does not store it,
 * and a task restored from the cache leaves the reference file missing,
 * so it is not packed into the resources of the source set.
 */
private fun GenerateProtoTask.declareReferenceFileOutput(descriptorsDir: File) {
    outputs.file(File(descriptorsDir, DescriptorSetReferenceFile.NAME))
        .withPropertyName(REFERENCE_FILE_PROPERTY)
}

/**
 * Declares the project version as an explicit input of this task so that
 * a version change invalidates the cached descriptor set.
 *
 * The descriptor set file name embeds the project version (see [descriptorSetFile]),
 * and the [reference file][DescriptorSetReferenceFile] written in the `doLast` action
 * points to that name. [GenerateProtoTask] is a cacheable task that keys its up-to-date
 * check and build-cache entry on the `.proto` sources and the compiler configuration
 * only — not on the project version or the names of the produced files.
 *
 * After a version-only change the Proto sources are unchanged, so the task is restored
 * from the build cache, bringing back a descriptor set produced for the previous version
 * while the reference file points to the new name. The mismatch leaves Protobuf types
 * unresolvable at runtime, surfacing as an `UnknownTypeException`.
 *
 * Declaring the version as an input makes the build cache regenerate the descriptor set
 * and its reference file when the version changes, while preserving caching for all other
 * changes. The value is read lazily so that it reflects the version resolved at execution
 * time, regardless of when `project.version` is assigned during configuration.
 */
private fun GenerateProtoTask.declareVersionInput() {
    inputs.property(
        DescriptorSetFilePlugin.VERSION_PROPERTY,
        project.provider { project.version.toString() }
    )
}

/**
 * Make the `processResources` task depend on this `GenerateProtoTask`.
 */
private fun GenerateProtoTask.dependOnProcessResourcesTask() {
    val ssn = SourceSetName(sourceSet.name)
    val processResources = JavaTaskName.processResources(ssn).value()
    // Find the task via iteration because the call to `named()` fails at
    // this project configuration stage.
    project.tasks.withType<ProcessResources>()
        .filter { it.name == processResources }
        .forEach { it.dependsOn(this) }
}
