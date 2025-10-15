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
import io.spine.code.proto.DescriptorSetReferenceFile
import io.spine.tools.code.SourceSetName
import io.spine.tools.gradle.protobuf.descriptorSetFile
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A Gradle project plugin that configures Protobuf generation tasks to produce
 * descriptor set files and expose them as resources of the corresponding source set.
 *
 * This plugin reproduces the behavior of `GenerateProtoTask.setupDescriptorSetFileCreation()`
 * defined in this repository's buildSrc utilities, but exposes it as a reusable plugin.
 */
public class DescriptorSetFilePlugin : Plugin<Project> {

    internal companion object {

        /**
         * The ID of this Gradle plugin.
         */
        const val id = "io.spine.descriptor-set-file"
    }

    override fun apply(project: Project) {
        //TODO:2025-10-15:alexander.yevsyukov: Move under the `generateProtoTasks` block.
        // Configure all Protobuf generate tasks directly (no reflection).
        project.tasks.withType(GenerateProtoTask::class.java).configureEach { task ->
            configureGenerateProtoTask(project, task)
        }
    }

    private fun configureGenerateProtoTask(project: Project, task: GenerateProtoTask) {
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
        // the descriptor set file and the reference file which is created in
        // the `doLast` block below are packed together with the class files.
        sourceSet.resources.srcDir(descriptorsDir.absolutePath)

        // Create a `desc.ref` file pointing to the descriptor file name once the task finishes.
        task.doLast {
            DescriptorSetReferenceFile.create(descriptorsDir, descriptorSetFile)
        }
    }
}
