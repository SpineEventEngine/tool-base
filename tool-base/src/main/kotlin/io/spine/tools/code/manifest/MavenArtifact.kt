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

package io.spine.tools.code.manifest

/**
 * A dependency on a software artifact.
 */
public data class MavenArtifact(val coordinates: String) : Dependency {

    /**
     * Creates an artifact with the given parts of the Maven coordinates.
     */
    public constructor(group: String, name: String, version: String) :
            this("$group$SEPARATOR$name$SEPARATOR$version")

    /**
     * The group to which the artifact belongs.
     */
    public val group: String

    /**
     * The ID of the artifact within the group.
     */
    public val name: String

    /**
     * The version of the artifact.
     */
    public val version: String

    init {
        val parts = coordinates.split(SEPARATOR)
        require(parts.size == 3) {
            "Maven coordinates must have 3 parts. Encountered: `$coordinates`."
        }

        fun requireNonEmpty(value: String, propName: String): String {
            require(value.isNotEmpty()) {
                "The `${propName}` part of Maven coordinates must not be empty." +
                        " Encountered: `$coordinates`."
            }
            return value
        }

        group = requireNonEmpty(parts[0], "group")
        name = requireNonEmpty(parts[1], "name")
        version = requireNonEmpty(parts[2], "version")
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
