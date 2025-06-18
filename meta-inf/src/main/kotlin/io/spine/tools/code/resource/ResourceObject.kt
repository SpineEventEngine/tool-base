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

import java.net.URL
import java.util.*

/**
 * Abstract base for objects stored in program resources.
 *
 *
 * Such a resource is represented by a string path relative to the `"resources"` directory
 * of a project, and is loaded by a specified [ClassLoader] on runtime.
 */
public abstract class ResourceObject internal constructor(
    protected val path: String,
    protected val classLoader: ClassLoader
) {

    private fun findUrl(): URL? {
        val url = classLoader.getResource(path)
        return url
    }

    /**
     * Checks if the resource with such a name exists in the classpath.
     *
     * @return `true` if the resource is present, `false` otherwise.
     */
    public fun exists(): Boolean {
        val resource = findUrl()
        return resource != null
    }

    /**
     * Obtains a [URL] of the resolved resource.
     *
     * If the resource cannot be resolved (i.e., the file does not exist),
     * throws an `IllegalStateException`.
     *
     * @return the resource URL
     */
    public fun locate(): URL {
        val url: URL? = findUrl()
        if (url == null) {
            throw cannotFind()
        }
        return url
    }

    /** Obtains the resource path of this resource object as passed on creation.  */
    public fun path(): String = path

    /**
     * Crate an exception stating that the resource cannot be found.
     */
    public fun cannotFind(): IllegalStateException {
        return IllegalStateException("Unable to find `$this`.")
    }

    /**
     * Enumerates all resources with the given path.
     */
    public fun resources(): Enumeration<URL> {
        return classLoader.getResources(path)
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is ResourceObject) {
            return false
        }
        return path == other.path
    }

    override fun toString(): String =
        "`$path` via `ClassLoader` `$classLoader`"
}
