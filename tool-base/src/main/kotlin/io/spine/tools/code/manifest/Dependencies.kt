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

import com.google.common.annotations.VisibleForTesting

/**
 * Dependencies of a software component.
 */
public class Dependencies(public val list: List<Dependency>){

    public companion object {

        /**
         * The separator of dependencies in the string form.
         *
         * @see toString
         * @see parse
         */
        public const val SEPARATOR: String = ","
        
        /**
         * Parses comma-separated list of dependencies, with each of them enclosed in
         * double quotes (").
         */
        @VisibleForTesting
        internal fun parse(value: String): Dependencies {
            if (value.isEmpty()) {
                return Dependencies(listOf())
            }
            val list: MutableList<Dependency> = mutableListOf()
            val deps = splitDeps(value)
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
     * Each dependency is enclosed in double quotes. If string form of a dependency has double
     * quotes within, they are escaped with leading backslashes. Dependencies are separated
     * with [commas][SEPARATOR].
     */
    override fun toString(): String {
        val result = list.joinToString(SEPARATOR) {
            val escaped = it.toString().escapeQuotes()
            "\"${escaped}\""
        }
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
}

private const val QUOTE: String = "\""
private const val QUOTE_ESCAPED = "\\\""
private fun String.escapeQuotes() = replace(QUOTE, QUOTE_ESCAPED)
private fun String.unescapeQuotes() = replace(QUOTE_ESCAPED, QUOTE)

/**
 * The regular expression for dependency enclosed in double quotes which may also have such
 * quotes escape. Does not accept empty strings, which are handled programmatically before this
 * regexp comes into play.
 *
 * For a detailed explanation of this regexp, please visit this
 * [blog post](https://www.metaltoad.com/blog/regex-quoted-string-escapable-quotes).
 * The only difference with the one described in the post is that this regexp handles only
 * double quotes.
 *
 * @see Dependencies.toString
 * @see Dependencies.parse
 */
private val quotedRegex = ".+".toRegex()
    //"((?<!\\\\)\")((?:.(?!(?<!\\\\)\\1))*.?)\\1".toRegex()

private fun splitDeps(value: String): List<String> {
    require(value.isNotEmpty())
    val deps = mutableListOf<String>()
    val matches = quotedRegex.findAll(value)
    matches.forEach { matchResult ->
        val dep = matchResult.groupValues[2]
        deps.add(dep.unescapeQuotes())
    }
    return deps
}

/**
 * Parses a dependency from the given string representation.
 *
 * @throws IllegalStateException if the given string does not start with a prefix of supported
 *          dependency format.
 */
private fun parseDependency(value: String): Dependency {
    if (value.startsWith(MavenArtifact.PREFIX)) {
        return MavenArtifact.parse(value)
    }
    if (value.startsWith(IvyDependency.PREFIX)) {
        return IvyDependency.parse(value)
    }
    throw IllegalStateException("Unsupported dependency format: `$value`.")
}
