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

/**
 * A test-only implementation of the [Dependency] interface that complements
 * the production [MavenArtifact] class which implements the same interface.
 *
 * The purpose of this class is to provide a quasi-production class with
 * a dependency string format that contains spaces and quotes.
 * This allows having meaningful tests for the [Dependencies] class
 * handing escaping of unusual characters.
 *
 * This class belongs to the production code because the [Dependencies] class
 * handles the `ivy:` format, delegating parsing to this class.
 *
 * @see Dependencies.parse
 */
internal data class IvyDependency(
    val org: String,
    val name: String,
    val rev: String
): Dependency {

    companion object {

        /**
         * The prefix of the dependency definition.
         */
        const val PREFIX: String = "ivy:"

        private val pattern = " org=\"(.+)\" name=\"(.+)\" rev=\"(.+)\"".toRegex()

        @Suppress("MagicNumber") // OK for regex group names.
        fun parse(value: String): IvyDependency {
            require(value.startsWith(PREFIX))
            val def = value.substring(PREFIX.length)
            val match = pattern.find(def)
            check(match != null) { "Unrecognized Ivy dependency format: `$def`." }
            val org = match.groupValues[1]
            val name = match.groupValues[2]
            val rev = match.groupValues[3]
            return IvyDependency(org, name, rev)
        }
    }

    override fun toString(): String = "$PREFIX org=\"$org\" name=\"$name\" rev=\"$rev\""
}
