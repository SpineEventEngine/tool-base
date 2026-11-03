/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
}
