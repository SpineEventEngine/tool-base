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

package io.spine.tools.jvm.maven

import kotlin.reflect.KProperty0

/**
 * A dependency on a software artifact stored in a Maven repository.
 *
 * @param group The group to which the artifact belongs.
 * @param name The ID of the artifact within the group.
 * @param version The version of the artifact.
 */
public data class MavenArtifact(
    public val group: String,
    public val name: String,
    public val version: String
) : Dependency {

    init {
        ::group.requireNonEmpty()
        ::name.requireNonEmpty()
        ::version.requireNonEmpty()    }

    /**
     * The Maven coordinates of this artifact in the format "group:name:version".
     */
    public val coordinates: String
        get() = "$group$SEPARATOR$name$SEPARATOR$version"

    /**
     * Creates an artifact from the given Maven coordinates string.
     */
    public constructor(coordinates: String) : this(
        parseGroup(coordinates),
        parseName(coordinates),
        parseVersion(coordinates)
    )

    public companion object {

        /**
         * The prefix to be used before [coordinates] in the string representation of
         * a Maven artifact dependency.
         */
        public const val PREFIX: String = "maven:"

        /**
         * The separator between [group], [name], and [version] parts of Maven coordinates.
         */
        public const val SEPARATOR: String = ":"


        /**
         * Number of parts in a Gradle-style notation representing a Maven artifact.
         *
         * For the sake of simplicity, we expect that a notation always has exactly 3 parts,
         * the group ID, the artifact name, and the version. Other possible configurations
         * are not supported.
         */
        private const val STRING_NOTATION_PARTS_COUNT = 3

        /**
         * Parses and validates the group part from Maven coordinates.
         */
        private fun parseGroup(coordinates: String): String {
            val parts = validateAndSplit(coordinates)
            return parts[0]
        }

        /**
         * Parses and validates the name part from Maven coordinates.
         */
        private fun parseName(coordinates: String): String {
            val parts = validateAndSplit(coordinates)
            return parts[1]
        }

        /**
         * Parses and validates the version part from Maven coordinates.
         */
        private fun parseVersion(coordinates: String): String {
            val parts = validateAndSplit(coordinates)
            return parts[2]
        }

        /**
         * Validates and splits Maven coordinates into parts.
         */
        private fun validateAndSplit(coordinates: String): List<String> {
            val parts = coordinates.split(SEPARATOR)
            require(parts.size == STRING_NOTATION_PARTS_COUNT) {
                "Maven coordinates must have $STRING_NOTATION_PARTS_COUNT parts. " +
                        "Encountered: `$coordinates`."
            }
            return parts
        }

        /**
         * Obtains the instance from the given string representation.
         *
         * @param value Maven coordinates with the leading [PREFIX].
         * @throws IllegalArgumentException if the given value does not start with [PREFIX].
         */
        public fun parse(value: String): MavenArtifact {
            require(value.startsWith(PREFIX))
            val coordinates = value.substring(PREFIX.length)
            return MavenArtifact(coordinates)
        }
    }

    /**
     * Obtains the string representation in the format:
     * `maven:<group>:<artifact>:<version>`.
     */
    override fun toString(): String = "$PREFIX$coordinates"
}

private fun KProperty0<String>.requireNonEmpty() {
    require(get().isNotEmpty()) {
        "The property `${name}` cannot be empty"
    }
}
