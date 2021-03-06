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
package io.spine.tools.gradle.testing

import io.spine.io.Copy.copyDir
import java.nio.file.Path
import java.util.function.Predicate
import kotlin.io.path.name

/**
 * The predicate to prevent copying unnecessary files when copying
 * the `buildSrc` directory from the parent project.
 *
 * The predicate:
 *  1) saves on unnecessary copying,
 *  2) prevents file locking issue under Windows, which fails the build because a file
 *     locked under the `.gradle` directory could not be copied.
 */
internal data class BuildSrcCopy(
    /**
     * If `true`, `buildSrc/build` directory will be copied.
     *
     * Such an arrangement is needed for test projects that refer to
     * the `io.spine.internal.dependency` package in their build scripts.
     * Such references are resoled if classes under `buildSrc/build/classes` are available
     * for the Gradle runner.
     */
    val includeBuildDir: Boolean = true
): Predicate<Path> {

    private val doNotCopy: List<String> = buildList(2) {
        add(".gradle")
        if (!includeBuildDir) {
            add("build")
        }
    }

    /** Copies the `buildSrc` directory from the [RootProject] into the specified directory. */
    fun writeTo(targetDir: Path) {
        val rootPath = RootProject.path()
        val buildSrc = rootPath.resolve("buildSrc")
        copyDir(buildSrc, targetDir) { path -> test(path) }
    }

    /** Tests if the given path should be copied. */
    override fun test(path: Path): Boolean {
        return path.any { doNotCopy.contains(it.name) }.not()
    }
}
