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

package io.spine.tools.meta

import io.spine.tools.meta.Dependencies.Companion.SEPARATOR
import io.spine.tools.meta.Dependencies.Companion.parse
import java.io.File
import java.nio.file.Files.write
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE

/**
 * Dependencies of a software component.
 *
 * The class allows [parsing][parse] string representations of dependencies.
 *
 * @param list The list of dependencies.
 *   The list must not contain two dependencies that are [MavenArtifact] with
 *   the same [module][Module].
 * @throws IllegalArgumentException if the list contains two dependencies with the same module.
 */
public class Dependencies(public val list: List<Dependency>) {

    init {
        validateNoDuplicateModules(list)
    }

    /**
     * Finds a dependency that represents the given module.
     *
     * @param module The module to find the dependency for.
     * @return the dependency representing the given module, or
     *   `null` if no such dependency exists.
     */
    public fun find(module: Module): Dependency? =
        list.filterIsInstance<MavenArtifact>()
            .find { it.module == module }

    public companion object {

        /**
         * The separator of dependencies in the string form.
         *
         * @see toString
         * @see parse
         */
        public const val SEPARATOR: String = "\n"

        /**
         * Parses a line-separated list of dependencies.
         *
         * Empty lines are allowed in the incoming string form for
         * the convenience of grouping for a human reader.
         * They will be filtered out.
         */
        internal fun parse(value: String): Dependencies {
            if (value.isEmpty()) {
                return Dependencies(listOf())
            }
            val list = mutableListOf<Dependency>()
            val deps = value.split(SEPARATOR).filter { it.isNotEmpty() }
            deps.forEach {
                val dep = parseDependency(it)
                list.add(dep)
            }
            return Dependencies(list)
        }
    }

    /**
     * Obtains a string form of the dependencies.
     *
     * Dependencies are separated with [new lines][SEPARATOR].
     */
    override fun toString(): String {
        val result = list.joinToString(SEPARATOR) { it.toString() }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Dependencies
        if (list != other.list) return false
        return true
    }

    override fun hashCode(): Int {
        return list.hashCode()
    }

    /**
     * Stores the dependencies in a text file, with each dependency on a separate line.
     *
     * @param file the file to store the dependencies in.
     * @throws IllegalArgumentException if the file is a directory.
     * @throws java.io.IOException if an I/O error occurs
     */
    public fun store(file: File) {
        require(!file.exists() || !file.isDirectory) {
            "Cannot store dependencies to the directory: `${file.absolutePath}`."
        }

        // Create parent directories if they don't exist.
        file.parentFile?.mkdirs()

        // Write each dependency on a separate line.
        val lines = list.map { it.toString() }
        write(file.toPath(), lines, CREATE, TRUNCATE_EXISTING, WRITE)
    }
}

/**
 * Parses a dependency from the given string representation.
 *
 * @throws IllegalStateException if the given string does not start with a prefix of
 *   a supported dependency format.
 */
internal fun parseDependency(value: String): Dependency = when {
    value.startsWith(MavenArtifact.PREFIX) -> MavenArtifact.parse(value)
    value.startsWith(IvyDependency.PREFIX) -> IvyDependency.parse(value)
    else -> error("Unsupported dependency format: `$value`.")
}

/**
 * Validates that there are no duplicate Maven modules in the given list of dependencies.
 *
 * @param list The list of dependencies to validate.
 * @throws IllegalArgumentException if two [MavenArtifact]s have the same [module][Module].
 */
private fun validateNoDuplicateModules(list: List<Dependency>) {
    val moduleToArtifacts = mutableMapOf<Module, MutableList<MavenArtifact>>()

    list.filterIsInstance<MavenArtifact>().forEach { artifact ->
        val module = artifact.module
        moduleToArtifacts.computeIfAbsent(module) { mutableListOf() }.add(artifact)
    }

    val duplicatedModules = moduleToArtifacts.filter { it.value.size > 1 }
    require(duplicatedModules.isEmpty()) {
        val duplicates = duplicatedModules.entries
            .joinToString("\n") { (module, duplicates) ->
                "Duplicated module: `$module`\n" +
                        "Artifacts:\n" +
                        duplicates.joinToString("\n") { "  - `$it`" }
            }
        "Artifacts with the same module found. Please correct the dependencies.\n" +
                duplicates
    }
}
