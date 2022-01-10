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

package io.spine.tools.gragle.testing

import com.google.common.truth.Truth.assertThat
import io.spine.tools.gradle.testing.GradleProject
import io.spine.tools.gradle.testing.GradleProjectSetup
import java.io.File
import java.nio.file.Files.exists
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class `'Sources' should` {

    companion object {
        private const val buildScr = "buildSrc"

        /** The name of the directory under `resources`. */
        private const val resourceDir = "sources_test"

        /** As defined under [resourceDir]. */
        val files = listOf(
            "src/main/java/Bar.java",
            "src/main/java/Foo.java",
            "src/main/proto/prod/code/empty.proto",
            "build.gradle.kts"
        )
    }

    private lateinit var projectDir: File
    private lateinit var setup: GradleProjectSetup

    @BeforeEach
    fun setUp(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()
        setup = GradleProject.setupAt(projectDir)
    }

    private fun resolve(path: String): Path = projectDir.toPath().resolve(path)

    @Nested
    inner class `copy 'buildScr'` {

        @Test
        fun `when instructed`() {
            setup.copyBuildSrc()
            setup.create()
            assertExists(buildScr)
        }

        @Test
        fun `only when told`() {
            setup.create()
            assertNotExists(buildScr)
        }
    }

    @Test
    fun `create a Gradle file with test environment variables`() {
        setup.create()
        assertExists(setup.testEnvPath())
    }

    @Nested
    inner class `copy files from resources` {

        @Test
        fun `when resource directory is specified`() {
            setup.fromResources(resourceDir)
            setup.create()
            for (fileName in files) {
                assertExists(fileName)
            }
        }

        @Test
        fun `only when resource directory is specified`() {
            setup.create()
            for (fileName in files) {
                assertNotExists(fileName)
            }
        }

        @Test
        fun `selecting files by given predicate`() {
            val predicate: (p: Path) -> Boolean = { path -> path.toString().contains(".java") }
            setup.fromResources(resourceDir, predicate)
            setup.create()
            files.filter { f -> predicate.invoke(Paths.get(f)) }
                .forEach(::assertExists)
            files.filter { f -> !(predicate.invoke(Paths.get(f))) }
                .forEach(::assertNotExists)
        }
    }

    private fun assertExists(file: Path) {
        assertTrue(exists(file), "`${file}` does not exist.")
    }

    private fun assertNotExists(file: Path) {
        assertFalse(exists(file), "`${file}` expected to NOT exist.")
    }

    /** Asserts that the path relative to the `projectDir` exists. */
    private fun assertExists(path: String) {
        val resolved = resolve(path)
        assertExists(resolved)
    }

    private fun assertNotExists(path: String) {
        val resolved = resolve(path)
        assertNotExists(resolved)
    }

    @Test
    fun `add files specified by name and content`() {
        val fileName = "foo.bar"
        val content = listOf( "fiz", "baz" )
        setup.addFile(fileName, content)
        setup.create()

        assertExists(fileName)
        val lines = resolve(fileName).toFile().readLines()
        assertThat(lines).containsExactlyElementsIn(content)
    }
}
