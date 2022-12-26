/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.gradle.protobuf

import io.spine.tools.code.SourceSetName
import io.spine.tools.fs.DescriptorsDir
import io.spine.tools.fs.DirectoryName
import io.spine.tools.gradle.project.artifact
import io.spine.tools.gradle.protobuf.ProtobufDependencies.sourceSetExtensionName
import io.spine.tools.gradle.project.sourceSet
import io.spine.tools.java.fs.DefaultJavaPaths
import io.spine.tools.resolve
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet

/**
 * Obtains an absolute path to the generated code directory of this project.
 *
 * The implementation of the getter of the property handles the transition to newer versions of
 * the Protobuf Gradle Plugin. Starting from v0.9.0 the plugin uses a project extension instead
 * of a Gradle convention object. This is so because using conventions were deprecated by Gradle
 * and is scheduled for removal in Gradle v8.0.
 *
 * Obtaining the value is done using reflection to avoid dependencies on both older and newer
 * versions of the API.
 */
public val Project.generatedFilesBaseDir: String
    get() = protobufGradlePluginAdapter.generatedFilesBaseDir

/**
 * Obtains the path to the directory which will be used for placing files generated
 * from proto definitions.
 *
 * If the value of the `generatedFilesBaseDir` property of the Protobuf Gradle Plugin has
 * the default value, returns the path to the `generated` directory under the project root dir.
 *
 * Otherwise, obtains the path configured in the `generatedFilesBaseDir` of the Protobuf Gradle
 * Plugin settings.
 */
public val Project.generatedDir: Path
    get() {
        if (usesDefaultGeneratedDir) {
            /*
               Ignore the default value specified by the plugin code because it "buries" the
               generated code under the `build` directory.

               We want the generated code more visible, and place it at the root of the project,
               so it is seen as a "sibling" with the `src` directory.

               This is the convention of our framework.
            */
            return projectDir.resolve(DirectoryName.generated).toPath()
        }

        /* If custom value was set by the programmer in the `protobuf` closure of the build
           script, use the specified path instead of the framework convention. */
        val fromExtension = generatedFilesBaseDir
        return Paths.get(fromExtension)
    }


/**
 * Tells if the `generatedFilesBaseDir` is set to the default value.
 *
 * For the default value, please see the constructor of
 * [com.google.protobuf.gradle.ProtobufExtension].
 */
internal val Project.usesDefaultGeneratedDir: Boolean
    get() {
        return generatedFilesBaseDir == "${buildDir}/generated/source/proto"
    }

/**
 * Attempts to find a source directory set named `proto` in the given source set.
 *
 * @return the found instance or `null` if the source set does not contain Protobuf code.
 */
public fun Project.protoDirectorySet(ssn: SourceSetName): SourceDirectorySet? {
    val sourceSet: SourceSet = sourceSet(ssn)
    val found = sourceSet.extensions.findByName(sourceSetExtensionName)
            as SourceDirectorySet?
    return found
}

/**
 * Obtains the directory into which descriptor set files are generated during the build.
 */
private val Project.descriptorsDir: DescriptorsDir
    get() = DefaultJavaPaths.at(projectDir.toPath()).buildRoot().descriptors()

/**
 * Obtains the descriptor set file for the specified source set of this project.
 */
public fun Project.descriptorSetFile(ssn: SourceSetName): File {
    val theArtifact = artifact(ssn)
    val descriptorSetFile = theArtifact.descriptorSetFile()
    val dir = descriptorsDir.forSourceSet(ssn.value)
    val path = descriptorSetFile.under(dir)
    return path.toFile()
}

/**
 * Obtains the path to this file resolved under the passed directory.
 */
private fun File.under(dir: Path): Path = dir.resolve(toString())
