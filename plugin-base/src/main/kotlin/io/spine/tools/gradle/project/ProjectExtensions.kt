/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.project

import com.google.protobuf.gradle.ProtobufConvention
import io.spine.tools.fs.DefaultPaths
import io.spine.tools.fs.DescriptorsDir
import io.spine.tools.fs.DirectoryName.generated
import io.spine.tools.gradle.Artifact
import io.spine.tools.gradle.ConfigurationName
import io.spine.tools.gradle.SourceSetName
import io.spine.tools.gradle.SourceSetName.Companion.main
import io.spine.tools.gradle.SourceSetName.Companion.test
import io.spine.tools.gradle.resolve
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

/**
 * Obtains the Java plugin extension of the project.
 */
public val Project.javaPluginExtension: JavaPluginExtension
    get() = extensions.getByType(JavaPluginExtension::class.java)

/**
 * Obtains source set container of the Java project.
 */
public val Project.sourceSets: SourceSetContainer
    get() = javaPluginExtension.sourceSets

/**
 * Obtains names of the source sets of this project.
 */
public val Project.sourceSetNames: List<SourceSetName>
    get() = sourceSets.map { s -> SourceSetName(s.name) }

/** Obtains a source set by the given name. */
public fun Project.sourceSet(name: String): SourceSet = sourceSets.getByName(name)

/** Obtains a source set by the given name. */
public fun Project.sourceSet(name: SourceSetName): SourceSet = sourceSets.getByName(name.value)

private fun Project.toArtifactBuilder(): Artifact.Builder =
    Artifact.newBuilder()
        .setGroup(group.toString())
        .setName(name)
        .setVersion(version.toString())

/**
 * Obtains the production [Artifact] of this project.
 */
public val Project.artifact: Artifact
    get() = toArtifactBuilder().build()

/**
 * Obtains the test [Artifact] of this project.
 */
public val Project.testArtifact: Artifact
    get() = toArtifactBuilder().useTestClassifier().build()

/**
 * Obtains the [Artifact] for the given source set.
 *
 * For the `main` source set, the call is equivalent to obtaining [Project.artifact].
 * For the `test` source set, the [Project.testArtifact] will be returned.
 *
 * For other source sets, the given name would be used as a classifier of the artifact.
 */
public fun Project.artifact(ssn: SourceSetName): Artifact {
    return when (ssn) {
        main -> artifact
        test -> testArtifact
        else -> toArtifactBuilder().setClassifier(ssn.value).build()
    }
}

/**
 * Attempts to find a source directory set named `proto` in the given source set.
 *
 * @return the found instance or `null` if the source set does not contain Protobuf code.
 */
public fun Project.protoDirectorySet(ssn: SourceSetName): SourceDirectorySet? {
    val sourceSet: SourceSet = sourceSet(ssn)
    val found = sourceSet.extensions.findByName("proto") as SourceDirectorySet?
    return found
}

/**
 * Obtains language-neutral instance of [DefaultPaths] for this project.
 */
private val Project.defaultPaths: DefaultPaths
    get() = DefaultPaths(projectDir.toPath())

/**
 * Obtains the directory into which descriptor set files are generated during the build.
 */
private val Project.descriptorsDir: DescriptorsDir
    get() = defaultPaths.buildRoot().descriptors()

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

@Suppress("DEPRECATION") /* We have to use `getConvention()` until
        Protobuf Gradle Plugin migrates to new API. */
public val Project.protobufConvention: ProtobufConvention
    get() = convention.getPlugin(ProtobufConvention::class.java)

/**
 * Tells if the `generatedFilesBaseDir` is set to the default value.
 *
 * For the default value, please see [com.google.protobuf.gradle.ProtobufConfigurator] constructor.
 */
internal val Project.conventionUsesDefaultGeneratedDir: Boolean
    get() {
    val fromConvention = protobufConvention.protobuf.generatedFilesBaseDir
    return fromConvention.equals("${buildDir}/generated/source/proto")
}

/**
 * Obtains the path to the directory which will be used for placing files generated
 * from proto definitions.
 */
public val Project.generatedDir: Path
    get() {
        if (conventionUsesDefaultGeneratedDir) {
            /*
               Ignore the default value specified by the plugin code because it "buries" the
               generated code under the `build` directory.

               We want the generated code more visible, and place it at the root of the project,
               so it is seen as a "sibling" with the `src` directory.

               This is the convention of our framework.
            */
            return projectDir.resolve(generated).toPath()
        }

        /* If custom value was set by the programmer in the `protobuf` closure of the build
           script, use the specified path instead of the framework convention. */
        val fromConvention = protobufConvention.protobuf.generatedFilesBaseDir
        return Paths.get(fromConvention)
    }

/** Obtains a configuration by its name. */
public fun Project.configuration(name: String): Configuration = configurations.getByName(name)

/** Obtains a configuration by its name. */
public fun Project.configuration(name: ConfigurationName): Configuration =
    configuration(name.value())
