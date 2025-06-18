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

import com.google.errorprone.annotations.CanIgnoreReturnValue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import java.util.function.Predicate
import java.util.stream.Collectors.toList

/**
 * Utilities for copy operations.
 */
internal object Copy {

    /**
     * Copies the directory and its contents matching the given predicate into another directory.
     *
     *
     * Both paths must point to existing directories.
     *
     *
     * The `dir` itself is copied as well. For example, if the `dir` path is
     * `/my/path/to/folder/foo` and the `target` path is `/my/other/folder`, as
     * a result of this operation, a `/my/other/folder/foo` directory will be created and all
     * the contents of the original `dir`, including nested directories, will be copied there.
     *
     * @param dir
     * the directory to copy
     * @param target
     * the new parent directory
     * @param matching
     * the predicate accepting the copied content
     */
    /**
     * Copies a whole directory and its contents into another directory.
     *
     *
     * Both paths must point to existing directories.
     *
     *
     * The `dir` itself is copied as well. For example, if the `dir` path is
     * `/my/path/to/folder/foo` and the `target` path is `/my/other/folder`, as
     * a result of this operation, a `/my/other/folder/foo` directory will be created and all
     * the contents of the original `dir`, including nested directories, will be copied there.
     *
     * @param dir
     * the directory to copy
     * @param target
     * the new parent directory
     */
    @JvmOverloads
    fun copyDir(
        dir: Path,
        target: Path,
        matching: Predicate<Path> = Predicate { path: Path -> true }
    ) {
        checkIsDirectory(dir)
        checkIsDirectory(target)
        doCopy(dir, target, matching, true)
    }

    /**
     * Copies the content of a directory into another directory.
     *
     *
     * Both paths must point to existing directories.
     *
     *
     * Files under the directory and all nested directories and files under them are copied
     * into the target directory. The directory itself is not copied.
     *
     * @param dir
     * the directory content of which will be copied
     * @param target
     * the new parent directory
     */
    fun copyContent(dir: Path, target: Path) {
        checkIsDirectory(dir)
        checkIsDirectory(target)
        doCopy(dir, target, { path: Path? -> true }, false)
    }

    /**
     * Copies the content of a directory matching the given predicate into another directory.
     *
     *
     * Both paths must point to existing directories.
     *
     * Files under the directory and all nested directories and files under them are copied
     * into the target directory. The directory itself is not copied.
     *
     * @param dir
     * the directory content of which will be copied
     * @param target
     * the new parent directory
     * @param matching
     * the predicate accepting the copied content
     */
    fun copyContent(dir: Path, target: Path, matching: Predicate<Path>) {
        checkIsDirectory(dir)
        checkIsDirectory(target)
        doCopy(dir, target, matching, false)
    }

    private fun doCopy(
        dir: Path,
        target: Path,
        matching: Predicate<Path>,
        withEnclosingDir: Boolean
    ) {
        val oldParent = if (withEnclosingDir)
            dir.parent
        else
            dir
        val paths: List<Path> = contentOf(dir, matching)
        for (path in paths) {
            val relative = oldParent.relativize(path)
            val newPath = target.resolve(relative)
            if (Files.isDirectory(path)) {
                if (!Files.exists(newPath)) {
                    Files.createDirectories(newPath)
                }
            } else if (Files.isRegularFile(path)) {
                val containingDir = newPath.getParent()
                if (!Files.exists(containingDir)) {
                    Files.createDirectories(containingDir)
                }
                Files.copy(path, newPath)
            }
        }
    }

    /**
     * Obtains all subdirectories and files enclosed the passed directory that match
     * the passed predicate.
     */
    private fun contentOf(dir: Path, matching: Predicate<Path>): List<Path> {
        val predicate: BiPredicate<Path, BasicFileAttributes> =
            BiPredicate { path: Path, attrs: BasicFileAttributes -> matching.test(path) }
        Files.find(dir, Int.Companion.MAX_VALUE, predicate).use { found ->
            val paths = found.collect(toList())
            return paths
        }
    }
}

@CanIgnoreReturnValue
internal fun checkIsDirectory(dir: Path): Path {
    require(Files.isDirectory(dir)) {
        "The path `$dir` is not a directory."
    }
    return dir
}
