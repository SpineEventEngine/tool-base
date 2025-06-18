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

package io.spine.tools.code.resource

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * A resource file in the classpath.
 *
 * @see file
 */
public class Resource private constructor(
    path: String,
    classLoader: ClassLoader
) : ResourceObject(path, classLoader) {

    /**
     * Obtains all the resource files by this path.
     *
     * The order in which the URLs are obtained is not defined.
     * If there are no such files, throws an `IllegalStateException`.
     *
     * @return the URLs to the resolved resource files
     */
    public fun locateAll(): MutableList<URL> {
        val resources = resourceEnumeration()
        val result = Collections.list(resources)
        if (result.isEmpty()) {
            throw cannotFind()
        }
        return result
    }

    private fun resourceEnumeration(): Enumeration<URL> {
        try {
            val resources = resources()
            return resources
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    /**
     * Obtains a new [InputStream] to the resource.
     *
     * The caller is responsible for closing the stream and handling I/O errors.
     * Throws an `IllegalStateException` if the resource cannot be resolved.
     *
     * @return new [InputStream]
     */
    public fun open(): InputStream {
        val resource = locate()
        try {
            return resource.openStream()
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    /**
     * Reads this resource as text.
     *
     *
     * Behaves similarly to [.open] but works with a character stream,
     * not with a byte stream.
     */
    private fun openAsText(charset: Charset): Reader {
        return InputStreamReader(open(), charset)
    }

    /**
     * Reads this resource as UTF-8 text.
     *
     *
     * Behaves similarly to [.open] but works with a character stream,
     * not with a byte stream.
     *
     * @see .openAsText
     */
    public fun openAsText(): Reader {
        return openAsText(StandardCharsets.UTF_8)
    }

    /**
     * Loads the whole resource file as a UTF-8 text file.
     *
     * @return the content of the resource file
     * @throws IllegalStateException
     * on a failure of opening the file, e.g., if the file does not exist
     */
    public fun read(): String {
        val stream = openAsText()
        val result = stream.readText()
        return result
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other is Resource) && super.equals(other)
    }

    public companion object {

        /**
         * Creates a new reference to a resource at the context of the given class loader.
         *
         * @param path The path to the resource file.
         * @param classLoader The class loader relative to which the resource is referenced.
         */
        public fun file(path: String, classLoader: ClassLoader): Resource {
            require(path.isNotBlank()) {
                "The path to the resource file cannot be blank. Encountered: `$path`."
            }
            return Resource(path, classLoader)
        }
    }
}
