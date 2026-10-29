/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package io.spine.tools.protobuf.gradle.plugin

import com.google.protobuf.gradle.GenerateProtoTask
import io.spine.code.proto.DescriptorSetReferenceFile
import io.spine.tools.code.SourceSetName
import io.spine.tools.protobuf.gradle.descriptorSetFile
import java.io.File

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
         * The name of the [GenerateProtoTask] input property holding the descriptor set
         * file name.
         */
        const val DESCRIPTOR_SET_NAME_PROPERTY = "descriptorSetName"
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
        //
        // The directory is added as built by this task, so that `processResources`,
        // and other tasks consuming the resources, depend on this task.
        // The dependency cannot be set on `processResources` directly without creating
        // the task: this function runs in the configuration action of `GenerateProtoTask`,
        // where Gradle forbids configuring other tasks lazily.
        sourceSet.resources.srcDir(project.files(descriptorsDir).builtBy(task))

        // Create a `desc.ref` file pointing to the descriptor file name once the task finishes.
        task.doLast {
            DescriptorSetReferenceFile.create(descriptorsDir, descriptorSetFile)
        }
        task.declareReferenceFileOutput(descriptorsDir)
        task.declareDescriptorSetNameInput(descriptorSetFile)
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
 * Declares the descriptor set file name as an explicit input of this task so that
 * a change to the name invalidates the cached descriptor set.
 *
 * The descriptor set file name is derived from the project's Maven coordinates — group,
 * artifact ID, version, and classifier (see [descriptorSetFile]) — and the
 * [reference file][DescriptorSetReferenceFile] written in the `doLast` action points to
 * that name. [GenerateProtoTask] is a cacheable task that keys its up-to-date check and
 * build-cache entry on the `.proto` sources and the compiler configuration only — not on
 * the names of the produced files.
 *
 * After a coordinate-only change — most commonly a version bump — the Proto sources are
 * unchanged, so the task is restored from the build cache, bringing back a descriptor set
 * produced under the previous name while the reference file points to the new name. The
 * mismatch leaves Protobuf types unresolvable at runtime, surfacing as an
 * `UnknownTypeException`.
 *
 * Declaring the descriptor set file name as an input makes the build cache regenerate the
 * descriptor set and its reference file whenever the name changes — covering version,
 * group, artifact ID, and classifier changes alike — while preserving caching for all
 * other changes.
 */
private fun GenerateProtoTask.declareDescriptorSetNameInput(descriptorSetFile: File) {
    inputs.property(DescriptorSetFilePlugin.DESCRIPTOR_SET_NAME_PROPERTY, descriptorSetFile.name)
}
