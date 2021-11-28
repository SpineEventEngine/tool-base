/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import com.google.common.testing.NullPointerTester
import io.spine.tools.gradle.testing.GradleProject
import io.spine.tools.gradle.testing.GradleProjectSetup
import java.io.File
import java.nio.file.Files.exists
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class `'GradleProjectSetup' should` {

    companion object {
        private const val origin = "sources_test"
    }

    private lateinit var projectDir: File
    private lateinit var setup: GradleProjectSetup

    @BeforeEach
    fun setUp(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()
        setup = GradleProject.setupAt(projectDir)
    }

    private fun resolve(path: String): Path = projectDir.toPath().resolve(path)
    private fun mainProto() = resolve("src/main/proto")
    private fun mainJava() = resolve("src/main/java")

    @Test
    fun `not accept 'null' arguments`(@TempDir tmpDir: File) {
        val instance = GradleProject.setupAt(tmpDir)
        NullPointerTester().testAllPublicInstanceMethods(instance)
    }

    @Test
    fun `create a Gradle file with test environment variables`() {
        setup.create()
        assertExists(setup.testEnvPath())
    }

    @Nested
    inner class `load files from resources` {

        @BeforeEach
        fun setOrigin() {
            setup.fromResources(origin)
        }

        @Test
        fun `by Java file names`() {
            val files = arrayOf("Foo.java", "Bar.java")
            setup.addJavaFiles(*files)
                .create()
            val mainJava = mainJava()
            for (fileName in files) {
                val file = mainJava.resolve(fileName)
                assertExists(file)
            }
        }

        @Test
        fun `by Protobuf file names`() {
            val protoFile = "prod/code/empty.proto"
            setup.addProtoFile(protoFile)
                .create()
            val mainProto = mainProto()
            assertExists(mainProto.resolve(protoFile))
        }
    }

    private fun assertExists(file: Path) {
        assertTrue(exists(file), "`${file}` does not exist.")
    }

    private fun assertExists(file: String) {
        val resolved: Path = resolve(file)
        assertExists(resolved)
    }
}
