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

package io.spine.tools.gradle.testing

import java.io.File

/**
 * A replacement to be made in a text.
 *
 * Scans the text for the occurrences of the token, such as `@MY_TOKEN@`,
 * and replaces them with the passed text value.
 *
 * This type uses the token names as-is. It is a responsibility of callers to provide
 * the correct name for the token with the respect of the format they consider to be legit.
 *
 * @param token the name of the token
 * @param value the value to replace the token occurrences to; may be blank
 */
public data class Replacement(public val token: String, public val value: String) {

    init {
        require(token.isNotBlank())
    }

    /**
     * Replaces all occurrences of [token][token].
     *
     * The passed [file] must exist and must not be a folder.
     */
    public fun replaceIn(file: File) {
        ensureFileAndExists(file)
        val original = file.readText()
        val modified = original.replace(token, value)
        if (modified != original) {
            file.writeText(modified)
        }
    }

    private fun ensureFileAndExists(file: File) {
        require(file.exists()) {
            "`Replacement` requires an existing file, but none found at `${file.absolutePath}`."
        }
        require(!file.isDirectory) {
            "`Replacement` cannot be launched in a directory `${file.absolutePath}`. " +
                    "Please pass a single file instead."
        }
    }
}
