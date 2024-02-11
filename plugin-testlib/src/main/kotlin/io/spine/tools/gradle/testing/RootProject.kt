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

import java.io.File
import java.nio.file.Files.exists
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Utilities for obtaining properties of a project which runs [GradleProject] in its tests.
 */
internal object RootProject {

    private const val VERSION_GRADLE = "version.gradle"
    private const val VERSION_GRADLE_KTS = "version.gradle.kts"

    /**
     * Finds a root of a project by presence of the [version.gradle][VERSION_GRADLE] or
     * [version.gradle.kts][VERSION_GRADLE_KTS] file.
     *
     * Starts from the current directory, climbing up, until the file is found. By convention
     * a project should have only one version file, which is placed in the root directory of
     * the project.
     *
     * @throws IllegalStateException
     *          if the [version.gradle.kts][VERSION_GRADLE] file is not found
     */
    @JvmStatic
    fun path(): Path {
        val workingFolderPath = Paths.get(".").toAbsolutePath()
        var extGradleDirPath: Path? = workingFolderPath
        while (extGradleDirPath != null && !hasVersionGradle(extGradleDirPath)) {
            extGradleDirPath = extGradleDirPath.parent
        }
        check(extGradleDirPath != null) {
            "Neither `${VERSION_GRADLE}` nor `${VERSION_GRADLE_KTS}` found in" +
                    " `${workingFolderPath}` or parent directories."
        }
        return extGradleDirPath
    }

    private fun hasVersionGradle(directory: Path): Boolean {
        val groovyScript = directory.resolve(VERSION_GRADLE)
        val kotlinScript = directory.resolve(VERSION_GRADLE_KTS)
        return exists(groovyScript) || exists(kotlinScript)
    }

    /**
     * Same as [.path], but returning `File` instance.
     *
     * @throws IllegalStateException
     * if the [version.gradle.kts][.VERSION_GRADLE] file is not found
     * @see .path
     */
    @JvmStatic
    fun dir(): File {
        return path().toFile()
    }

    /**
     * Returns a path to a conventionally established temp directory named `.gradle-test-kit`,
     * for Gradle TestKit runners.
     *
     * It may be used to configure TestKit runners, so that just a single folder
     * is created for all integration tests.
     */
    @JvmStatic
    fun testKitTempDir() : Path {
        return path().resolve(".gradle-test-kit")
    }
}
