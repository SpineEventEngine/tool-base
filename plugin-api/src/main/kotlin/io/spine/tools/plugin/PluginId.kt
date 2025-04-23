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

package io.spine.tools.plugin

import org.jetbrains.annotations.VisibleForTesting

/**
 * An identifier of a plugin.
 *
 * The given [value] must satisfy the requirements checked by the [isValid] function.
 * Otherwise, [IllegalArgumentException] will be thrown.
 *
 * **API Note**: Even though the requirements are modeled after
 * the [Gradle plugin IDs](https://bit.ly/gradle-plugin-id-policy), the API is not intended
 * only for Gradle plugins. We find them reasonable for identifying components in various
 * pluggable environments.
 */
public data class PluginId(val value: String) {

    init {
        require(isValid(value)) { "Invalid plugin ID: `$value`." }
    }

    override fun toString(): String = value

    internal companion object {

        /**
         * The part separator in the ID value.
         */
        private const val SEPARATOR = '.'

        /**
         * Must start with a lowercase letter and contain only lowercase letters,
         * digits, `'.'`, and `'-'`.
         */
        private val allowedCharsRegex by lazy {
            Regex("^[a-z][a-z0-9.-]*\$")
        }

        /**
         * Plugin names forbidden by the Gradle
         * [plugin ID policy](https://bit.ly/gradle-plugin-id-policy).
         */
        private val forbiddenNamespaces = listOf(
            "org.gradle",
            "com.gradle",
            "com.gradleware"
        )

        /**
         * Validates the given [id] to satisfy the naming constraints.
         *
         * The ID must satisfy the following conditions:
         *  1. Start with a lowercase letter.
         *  2. Contain only lowercase letters, digits, `'.'`, and `'-'`.
         *  3. Must contain at least one '`.'` character.
         *  4. Cannot contain consecutive `'.'` characters.
         *  5. Cannot start or end with a `'.'`.
         *  6. Must not start from "com.gradle"`, `"org.gradle"`, or `"org.gradleware"`.
         */
        @VisibleForTesting
        @Suppress("ReturnCount")
        fun isValid(id: String): Boolean {
            if (!allowedCharsRegex.matches(id)) {
                return false
            }
            if (!id.contains(SEPARATOR)) {
                return false
            }
            if (id.startsWith(SEPARATOR) || id.endsWith(SEPARATOR)) {
                return false
            }
            if ("$SEPARATOR$SEPARATOR" in id) {
                return false
            }
            if (forbiddenNamespaces.any { id.startsWith(it) }) {
                return false
            }
            return true
        }
    }
}
