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

package io.spine.tools.gradle.protobuf

import com.google.protobuf.gradle.ProtobufExtension
import io.spine.tools.code.SourceSetName
import io.spine.tools.fs.DescriptorsDir
import io.spine.tools.fs.DirectoryName
import io.spine.tools.gradle.project.artifact
import io.spine.tools.gradle.project.sourceSet
import io.spine.tools.gradle.protobuf.ProtobufDependencies.sourceSetExtensionName
import io.spine.tools.java.fs.DefaultJavaPaths
import io.spine.tools.resolve
import java.io.File
import java.nio.file.Path
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet

/**
 * Obtains the extension of Protobuf Gradle Plugin in the given project.
 */
public val Project.protobufExtension: ProtobufExtension?
    get() = extensions.findByType(ProtobufExtension::class.java)

/**
 * Obtains the directory where the Protobuf Gradle Plugin should place the generated code.
 *
 * The directory is fixed to be `$buildDir/generated/source/proto` and cannot be
 * changed by the settings of the plugin. Even though [ProtobufExtension] has a property
 * [generatedFilesBaseDir][ProtobufExtension.getGeneratedFilesBaseDir], which is supposed
 * to be used for this purpose, it is declared with `@PackageScope` and thus cannot be
 * accessed from outside the plugin. The Protobuf Gradle Plugin (at v0.9.2) does not
 * modify the value of the property either.
 */
public val Project.generatedSourceProtoDir: Path
    get() = layout.buildDirectory.dir("generated/source/proto").get().asFile.toPath()

/**
 * Obtains the path to the directory which will be used for placing files generated
 * from proto definitions.
 */
public val Project.generatedDir: Path
    get() = projectDir.resolve(DirectoryName.generated).toPath()

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
 * Obtains the directory containing generated Java source code for the specified source set.
 */
public fun Project.generatedJavaDir(ssn: SourceSetName): Path =
    generated(ssn).resolve(DirectoryName.java)

/**
 * Obtains the directory with the generated gRPC code for the specified source set.
 */
public fun Project.generatedGrpcDir(ssn: SourceSetName): Path =
    generated(ssn).resolve(DirectoryName.grpc)

/**
 * Obtains the path to the source set under `$projectDir/generated`.
 */
public fun Project.generated(ssn: SourceSetName): Path {
    return generatedDir.resolve(ssn.value)
}

/**
 * Obtains the path to this file resolved under the passed directory.
 */
private fun File.under(dir: Path): Path = dir.resolve(toString())
