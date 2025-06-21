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

package io.spine.tools.dependency

import io.spine.tools.jvm.resource.Resource
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE

/**
 * A class that associates a Maven artifact with its dependencies.
 *
 * @param artifact The Maven artifact.
 * @param dependencies The dependencies of the artifact.
 */
public data class ArtifactDependencies(
    public val artifact: MavenArtifact,
    public val dependencies: Dependencies
) {

    /**
     * Returns the identifier of the [artifact] with the [MavenArtifact.PREFIX].
     */
    override fun toString(): String = artifact.toString()

    /**
     * Stores the artifact dependencies in a text file.
     *
     * The artifact itself is always the first line, followed by its dependencies.
     *
     * @param file The file to store the artifact dependencies in.
     * @throws IllegalArgumentException if the file is a directory.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public fun store(file: File) {
        require(!file.exists() || !file.isDirectory) {
            "Cannot store artifact dependencies to the directory: `${file.absolutePath}`."
        }

        file.parentFile?.mkdirs()

        val lines = mutableListOf(artifact.toString())
        lines.addAll(dependencies.list.map { it.toString() })

        Files.write(file.toPath(), lines, CREATE, TRUNCATE_EXISTING, WRITE)
    }

    public companion object {

        /**
         * The extension of the dependency file.
         */
        public const val FILE_EXTENSION: String = ".deps"

        /**
         * The directory where dependency files are stored.
         */
        public const val RESOURCE_DIRECTORY: String = "META-INF/io.spine"

        /**
         * Creates an error message for when artifact dependencies cannot be loaded.
         *
         * @param wrongInput The description of why the input is wrong.
         * @param source The source of the artifact dependencies.
         * @return the error message.
         */
        internal fun cannotLoad(wrongInput: String, source: String): String =
            "Cannot load artifact dependencies from $wrongInput $source."

        /**
         * Loads artifact dependencies from a file.
         *
         * @param file The file to load the artifact dependencies from.
         * @return the loaded artifact dependencies.
         * @throws IllegalArgumentException if the file does not exist or is a directory.
         * @throws java.io.IOException if an I/O error occurs.
         * @throws IllegalStateException if the file is empty or is not of the expected format.
         * @see store
         */
        public fun load(file: File): ArtifactDependencies {
            val source = "file: `${file.absolutePath}`"
            require(file.exists()) {
                cannotLoad("non-existent", source)
            }
            require(!file.isDirectory) {
                cannotLoad("a directory:", source)
            }

            val lines = Files.readAllLines(file.toPath())
            return parseLines(lines, source)
        }

        /**
         * Loads artifact dependencies from a resource.
         *
         * @param path The path to the resource.
         * @param classLoader The class loader to use for loading the resource.
         * @return the loaded artifact dependencies.
         * @throws IllegalStateException if the resource does not exist, is empty,
         *   or is not of the expected format.
         * @see store
         */
        public fun loadFromResource(path: String, classLoader: ClassLoader): ArtifactDependencies {
            val resource = Resource.file(path, classLoader)
            val content = resource.read()
            val lines = content.lines().filter { it.isNotBlank() }
            return parseLines(lines, "resource: `$path`")
        }

        /**
         * Loads artifact dependencies from a resource.
         *
         * @param path the path to the resource.
         * @param cls the class to use for loading the resource.
         * @return the loaded artifact dependencies.
         * @throws IllegalStateException if the resource does not exist, is empty,
         *   or is not of the expected format.
         * @see store
         */
        public fun loadFromResource(path: String, cls: Class<*>): ArtifactDependencies =
            loadFromResource(path, cls.classLoader)

        /**
         * Loads artifact dependencies from a resource for the given module.
         *
         * @param module the module to load the artifact dependencies for.
         * @param classLoader the class loader to use for loading the resource.
         * @return the loaded artifact dependencies.
         * @throws IllegalStateException if the resource does not exist, is empty,
         *   or is not of the expected format.
         * @see store
         */
        public fun loadFromResource(
            module: Module,
            classLoader: ClassLoader
        ): ArtifactDependencies {
            val resourcePath = "$RESOURCE_DIRECTORY/${module.fileSafeId}$FILE_EXTENSION"
            return loadFromResource(resourcePath, classLoader)
        }

        /**
         * Loads artifact dependencies from a resource for the given module.
         *
         * @param module The module to load the artifact dependencies for.
         * @param cls The class to use for loading the resource.
         * @return the loaded artifact dependencies.
         * @throws IllegalStateException if the resource does not exist, is empty,
         *   or is not of the expected format.
         * @see store
         */
        public fun loadFromResource(module: Module, cls: Class<*>): ArtifactDependencies =
            loadFromResource(module, cls.classLoader)
    }
}

/**
 * Parses the given lines into an [ArtifactDependencies] instance.
 *
 * @param lines the lines to parse.
 * @param source the source of the lines, used for error messages.
 * @return the parsed artifact dependencies.
 * @throws IllegalStateException if the lines are empty or are not of the expected format.
 */
private fun parseLines(lines: List<String>, source: String): ArtifactDependencies {
    require(lines.isNotEmpty()) {
        ArtifactDependencies.cannotLoad("the empty", source)
    }

    val artifactLine = lines[0]
    require(artifactLine.startsWith(MavenArtifact.PREFIX)) {
        "The first line of the $source must be a Maven artifact." +
                " Encountered: `$artifactLine`."
    }
    val artifact = MavenArtifact.parse(artifactLine)

    val dependencyLines = lines.subList(1, lines.size)
    val dependencies = if (dependencyLines.isEmpty()) {
        Dependencies(emptyList())
    } else {
        val dependencyList = dependencyLines.map { parseDependency(it) }
        Dependencies(dependencyList)
    }

    return ArtifactDependencies(artifact, dependencies)
}

/**
 * Parses a dependency from the given string representation.
 *
 * @throws IllegalStateException if the given string does not start with a prefix of
 *   a supported dependency format.
 */
private fun parseDependency(value: String): Dependency = when {
    value.startsWith(MavenArtifact.PREFIX) -> MavenArtifact.parse(value)
    value.startsWith(IvyDependency.PREFIX) -> IvyDependency.parse(value)
    else -> error("Unsupported dependency format: `$value`.")
}
