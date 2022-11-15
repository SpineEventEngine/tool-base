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

package io.spine.tools.code

import io.spine.tools.titlecaseFirstChar

/**
 * A base for names of Gradle project objects that are based on a name of a source set.
 */
public open class SourceSetBasedName protected constructor(

    /**
     * A formatting string which refers the source set using [SourceSetName.toPrefix]
     * of the task name has it at the beginning of the name, or by [SourceSetName.toInfix]
     * if the name of the source set comes in the middle.
     */
    private val value: String,

    /**
     * The name of the source set to which this object belongs.
     */
    public val sourceSetName: SourceSetName
) {

    /** Obtains the string value of this name. */
    public fun name(): String = value

    /** Obtains the string value of this name. */
    override fun toString(): String = value

    override fun hashCode(): Int = value.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as SourceSetBasedName
        return value == other.value
                // Just comparing values should be enough for equality because the `value` property
                // refers to a name of a source set.
                // This comparison is a safety measure for the cases similar to
                // the `SourceSetName.main` which returns empty string in prefix and infix forms.
                // Other cases for `SourceSetName` introduced in the future may break our
                // current assumptions for equality. Comparing both properties keeps us at the
                // safe side.
                && sourceSetName == other.sourceSetName
    }

    public companion object {

        /**
         * Obtains a suffix for a name, assuming that the passed name of a source set
         * is used in the prefix form.
         *
         * If the source set name prefix form is an empty string, the given [value] is returned.
         * If the prefix form of the source set name is not an empty string, the first
         * character of the `value` is [titlecassed][String.titlecaseFirstChar] in
         * the returned value.
         */
        public fun suffix(ssnAsPrefix: SourceSetName, value: String): String =
            if (ssnAsPrefix.toPrefix().isEmpty())
                value
            else
                value.titlecaseFirstChar()
    }
}
