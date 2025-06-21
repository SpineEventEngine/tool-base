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

package io.spine.tools.io

import com.google.errorprone.annotations.CanIgnoreReturnValue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Preconditions for I/O operations.
 */
public object IoPreconditions {

    /**
     * Ensures that the passed file exists.
     *
     * @return the passed file if it exists.
     * @throws IllegalStateException if the file is missing.
     */
    @CanIgnoreReturnValue
    public fun checkExists(file: File): File {
        check(file.exists()) { "The file `$file` does not exist." }
        return file
    }

    /**
     * Ensures that the file with the passed path exists.
     *
     * @return the passed path if it exists.
     * @throws IllegalArgumentException if the path does not exist.
     */
    @CanIgnoreReturnValue
    public fun checkExists(path: Path): Path {
        check(path.exists()) { "The path `$path` does not exist." }
        return path
    }

    /**
     * Ensures that the passed path is a directory.
     *
     * @return the passed path if it represents a directory
     * @throws IllegalArgumentException
     * if the path is not a directory
     */
    @CanIgnoreReturnValue
    @Throws(IllegalArgumentException::class)
    public fun checkIsDirectory(dir: Path): Path {
        checkNotNull(dir)
        require(Files.isDirectory(dir)) { "The path `$dir` is not a directory." }
        return dir
    }

    /**
     * Ensures that the passed `File` is not an existing directory.
     */
    @CanIgnoreReturnValue
    public fun checkNotDirectory(file: File): File {
        if (file.exists() && file.isDirectory()) {
            error("File expected, but a directory found: `${file.getAbsolutePath()}`.")
        }
        return file
    }
}
