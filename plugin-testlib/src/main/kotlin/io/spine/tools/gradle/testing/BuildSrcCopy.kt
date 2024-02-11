/*
 * Copyright 2024, TeamDev. All rights reserved.
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
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Predicate
import kotlin.io.path.div
import kotlin.io.path.exists
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
     * If `true`, `buildSrc/build/libs/buildSrc.jar` will be copied
     * to the root of destination-`buildSrc` directory.
     *
     * This JAR file may be included into `buildSrc/build.gradle.kts`
     * as an implementation-level dependency, and in this way, replace
     * all the source files, which otherwise would have had to be compiled
     * from scratch.
     *
     * Testing shows ~50% reduce in integration test time,
     * when this approach is used.
     */
    val includeBuildSrcJar: Boolean = true,

    /**
     * If `true`, `buildSrc/src` directory will be copied.
     *
     * This approach is alternative to using `buildSrc.jar`,
     * and is known to be slower, as additional Kotlin compilation
     * is going to be required for these source files.
     *
     * See [includeBuildSrcJar].
     */
    val includeSourceDir: Boolean = false,

    /**
     * If `true`, `buildSrc/build` directory will be copied.
     *
     * Such an arrangement is needed for test projects that refer to
     * the `io.spine.internal.dependency` package in their build scripts.
     * Such references are resoled if classes under `buildSrc/build/classes` are available
     * for the Gradle runner.
     *
     * So far, all "field" tests have shown that this directory
     * **cannot** be re-used, as its contents will be regenerated anyway,
     * because Kotlin compiler detects the paths of source files
     * in the "copied" version of `buildSrc/src` to be different
     * from those used when `buildSrc/build` contents were first obtained.
     */
    val includeBuildDir: Boolean = false,
): Predicate<Path> {

    companion object {
        /**
         * Name of the `buildSrc.jar` file.
         *
         * Exposed to be re-used in tests.
         */
        internal const val JAR_NAME = "buildSrc.jar"

        /**
         * Name of the `buildSrc` folder.
         *
         * Exposed for testing.
         */
        internal const val FOLDER_NAME = "buildSrc"
    }

    private val doNotCopy: List<String> = buildList(3) {
        add(".gradle")
        if (!includeBuildDir) {
            add("build")
        }
        if (!includeSourceDir) {
            add("src")
        }
    }

    /** Copies the `buildSrc` directory from the [RootProject] into the specified directory. */
    fun writeTo(targetDir: Path) {
        val rootPath = RootProject.path()
        val buildSrc = rootPath.resolve(FOLDER_NAME)
        copyDir(buildSrc, targetDir) { path -> test(path) }
        copyJar(rootPath, targetDir)
    }

    private fun copyJar(rootPath: Path, targetDir: Path) {
        val jar = rootPath / FOLDER_NAME / "build" / "libs" / JAR_NAME
        if (includeBuildSrcJar && jar.exists()) {
            val jarTarget = targetDir / FOLDER_NAME / JAR_NAME
            Files.copy(jar, jarTarget)
        }
    }

    /**
     * Tests if the given path should be copied.
     *
     * This method does not account for `buildSrc.jar`,
     * as the folder structure for its target location (`<targetDir>/buildSrc`)
     * from the original folder structure (`<rootDir>/buildSrc/build/libs/buildSrc.jar`).
     **/
    override fun test(path: Path): Boolean {
        return path.any { doNotCopy.contains(it.name) }.not()
    }
}
