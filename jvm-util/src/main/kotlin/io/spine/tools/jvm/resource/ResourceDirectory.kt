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

package io.spine.tools.jvm.resource

import java.net.URISyntaxException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Predicate
import kotlin.io.path.exists

/**
 * A directory with resources in the classpath.
 *
 * @see get
 * @see Resource.file
 */
public class ResourceDirectory private constructor(
    path: String,
    classLoader: ClassLoader
) : ResourceObject(path, classLoader) {

    /**
     * Obtains the path to this directory under resources.
     */
    public fun toPath(): Path {
        val url = locate()
        try {
            val result = Paths.get(url.toURI())
            return result
        } catch (e: URISyntaxException) {
            throw IllegalStateException(e)
        }
    }

    /**
     * Copies the content of the directory to the target directory.
     *
     * @param target The path to an existing directory on the file system.
     * @see copyContentTo
     */
    public fun copyContentTo(target: Path) {
        checkTarget(target)
        copyContentTo(target) { path: Path -> true }
    }

    /**
     * Copies the content of the directory matching the condition to the target directory.
     *
     * @param matching The condition for accepting the copied content.
     * @param target The path to an existing directory on the file system.
     * @see copyContentTo
     */
    public fun copyContentTo(target: Path, matching: Predicate<Path>) {
        checkTarget(target)
        val from = toPath()
        Copy.copyContent(from, target, matching)
    }

    /**
     * Copies this directory to the target directory.
     *
     * @param target The path to an existing directory on the file system.
     * @see copyContentTo
     */
    public fun copyTo(target: Path) {
        checkTarget(target)
        copyTo(target) { path: Path -> true }
    }

    /**
     * Copies this directory and its content matching the condition to another directory.
     *
     * @param target
     * the path to existing directory on the file system
     * @see .copyContentTo
     * @see Copy.copyDir
     */
    public fun copyTo(target: Path, matching: Predicate<Path>) {
        checkTarget(target)
        val from = toPath()
        Copy.copyDir(from, target, matching)
    }


    public override fun hashCode(): Int {
        return path().hashCode()
    }

    public override fun equals(other: Any?): Boolean {
        return (other is ResourceDirectory) && super.equals(other)
    }

    public companion object {
        /**
         * Creates a new reference to a resource directory at the context of the given class loader.
         *
         * @param path
         * the path to the resource directory
         * @param classLoader
         * the class loader relative to which the resource directory is referenced
         */
        public fun get(path: String, classLoader: ClassLoader): ResourceDirectory {
            require(path.isNotBlank())
            return ResourceDirectory(path, classLoader)
        }

        private fun checkTarget(target: Path) {
            checkNotNull(target)
            require(target.exists()) {
                "The target directory does not exist: `$target`."
            }
        }
    }
}
