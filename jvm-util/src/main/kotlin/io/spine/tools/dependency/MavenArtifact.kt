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

import io.spine.tools.dependency.MavenArtifact.Companion.PREFIX
import io.spine.tools.jvm.requireNonEmpty

/**
 * A dependency on a software artifact stored in a Maven repository.
 *
 * @param group The group to which the artifact belongs.
 * @param name The ID of the artifact within the group.
 * @param version The version of the artifact.
 * @param classifier The classifier of the artifact, if any.
 * @param extension The extension of the artifact, if any.
 */
public data class MavenArtifact(
    public val group: String,
    public val name: String,
    public val version: String,
    public val classifier: String? = null,
    public val extension: String? = null
) : Dependency {

    init {
        ::group.requireNonEmpty()
        ::name.requireNonEmpty()
        ::version.requireNonEmpty()    }

    /**
     * The Maven coordinates of this artifact in the format "group:name:version[:classifier][@extension]".
     */
    public val coordinates: String
        get() {
            val result = StringBuilder("$group$SEPARATOR$name$SEPARATOR$version")
            classifier?.let { result.append("$SEPARATOR$it") }
            extension?.let { result.append("@$it") }
            return result.toString()
        }

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
         * Minimum number of parts in a Gradle-style notation representing a Maven artifact.
         *
         * A notation must have at least 3 parts: the group ID, the artifact name, and the version.
         * It may also include a classifier and an extension.
         */
        private const val MIN_STRING_NOTATION_PARTS_COUNT = 3

        /**
         * Validates and splits Maven coordinates into parts.
         */
        private fun validateAndSplit(coordinates: String): Pair<List<String>, String?> {
            // Split by @ to separate extension if present
            val mainParts = coordinates.split("@", limit = 2)
            val mainCoordinates = mainParts[0]
            val extension = if (mainParts.size > 1) mainParts[1] else null

            // Split the main coordinates by :
            val parts = mainCoordinates.split(SEPARATOR)
            require(parts.size >= MIN_STRING_NOTATION_PARTS_COUNT) {
                "Maven coordinates must have at least $MIN_STRING_NOTATION_PARTS_COUNT parts. " +
                        "Encountered: `$coordinates`."
            }
            return Pair(parts, extension)
        }

        /**
         * Creates an artifact from the given Maven coordinates string.
         */
        public fun withCoordinates(coordinates: String): MavenArtifact {
            val (parts, extension) = validateAndSplit(coordinates)
            return MavenArtifact(
                group = parts[0],
                name = parts[1],
                version = parts[2],
                classifier = if (parts.size > 3) parts[3] else null,
                extension = extension
            )
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
            return withCoordinates(coordinates)
        }
    }

    /**
     * Obtains the string representation in the format:
     * `maven:<group>:<artifact>:<version>`.
     */
    override fun toString(): String = "$PREFIX$coordinates"
}
