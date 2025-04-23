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

package io.spine.tools.gradle.testing

import io.spine.io.ResourceDirectory
import io.spine.util.Exceptions.illegalStateWithCauseOf
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.createDirectories
import java.nio.file.Files.write
import java.nio.file.Path

/**
 * Handles writing all source code for a new [GradleProject].
 */
internal class Sources(private val setup: GradleProjectSetup) {

    private val projectDir: Path =  setup.projectDir.toPath()

    /** Creates source code files for a new [GradleProject].  */
    fun write() {
        copyBuildSrc()
        createTestEnv()
        copyFromResources()
        createFiles()
    }

    private fun copyBuildSrc() {
        setup.buildSrcCopy?.writeTo(projectDir)
    }

    private fun createTestEnv() {
        val testEnvGradle = TestEnvGradle(projectDir)
        testEnvGradle.createFile()
    }

    private fun copyFromResources() {
        if (resourceOriginAssigned()) {
            val classLoader = javaClass.classLoader
            val directory = ResourceDirectory.get(origin(), classLoader)
            directory.copyContentTo(projectDir, setup.matching)
        }
    }

    private fun resourceOriginAssigned(): Boolean = setup.resourceDir != null

    private fun origin(): String {
        check(resourceOriginAssigned()) {
            "The project is not configured to load files from resources." +
                    " Please call `${GradleProjectSetup::class.java.simpleName}.setOrigin(String)`."
        }
        return setup.resourceDir!!
    }

    private fun createFiles() {
        setup.filesToCreate
            .forEach { (path: String, lines: Iterable<String>) -> createFile(path, lines) }
    }

    private fun createFile(path: String, lines: Iterable<String>) {
        val sourcePath = resolve(path)
        try {
            createDirectories(sourcePath.parent)
            write(sourcePath, lines, UTF_8)
        } catch (e: IOException) {
            throw illegalStateWithCauseOf(e)
        }
    }

    private fun resolve(path: String): Path = projectDir.resolve(path)
}
