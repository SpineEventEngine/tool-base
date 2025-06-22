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

package io.spine.tools.meta

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.testing.TestValues.randomString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("`Dependencies` should")
internal class DependenciesSpec {

    @Test
    fun `escapes quotes in string form`() {
        val group = "io.spine.tools"
        val name = "tool-base"
        val ver = "2.0.0"

        val ivyDep = IvyDependency(group, name, ver)
        val escaped = ivyDep.toString().replace("\"", "\\\"")

        val deps = Dependencies(listOf(ivyDep))
        val depsStr = deps.toString()

        depsStr shouldBe "\"${escaped}\""
    }

    /**
     * Imaginary case of something unusual in the version component, including comma and
     * space characters to test parsing against spaces and commas inside coordinates.
     */
    private fun unusualVersion(): String = randomString() + ", and then, some"

    @Test
    fun `reject dependencies with duplicate modules and provide detailed error message`() {
        val group = "io.spine.tools"
        val name = "tool-base"
        val version1 = "1.0.0"
        val version2 = "2.0.0"

        val dep1 = MavenArtifact(group, name, version1)
        val dep2 = MavenArtifact(group, name, version2)
        val module = Module(group, name)

        val exception = assertThrows<IllegalArgumentException> {
            Dependencies(listOf(dep1, dep2))
        }

        val errorMessage = exception.message ?: ""

        errorMessage.let {
            it shouldContain "Artifacts with the same module found."
            it shouldContain "Duplicated module: `$module`"
            it shouldContain dep1.toString()
            it shouldContain dep2.toString()
        }
    }

    @Nested
    inner class Parse {

        @Test
        fun `empty string`() {
            Dependencies.parse("").list shouldBe emptyList()
        }

        @Test
        fun `one Maven dependency`() {
            val group = randomString()
            val name = randomString()
            val version = unusualVersion()
            val ma = MavenArtifact.withCoordinates("$group:$name:$version")
            val depStr = "\"$ma\""

            val deps = Dependencies.parse(depStr)

            deps.list.let {
                it shouldHaveSize 1
                it[0] shouldBe ma
            }
        }

        @Test
        fun `several dependencies`() {
            val original = listOf(
                MavenArtifact("io.spine.tools", "tool-base", "2.0.0"),
                IvyDependency("org.gradle", "wrapper", "7.4.2"),
                MavenArtifact("io.spine", "core-java", "2.0.1"),
            )
            val str = Dependencies(original).toString()

            val parsed = Dependencies.parse(str).list

            parsed shouldBe original
        }
    }

    @Nested
    inner class Find {

        @Test
        fun `existing module`() {
            val group = "io.spine.tools"
            val name = "tool-base"
            val version = "2.0.0"
            val module = Module(group, name)
            val artifact = MavenArtifact(group, name, version)
            val ivyDep = IvyDependency("org.gradle", "wrapper", "7.4.2")

            val dependencies = Dependencies(listOf(artifact, ivyDep))

            val found = dependencies.find(module)

            found shouldBe artifact
        }

        @Test
        fun `non-existing module`() {
            val artifact = MavenArtifact("io.spine.tools", "tool-base", "2.0.0")
            val ivyDep = IvyDependency("org.gradle", "wrapper", "7.4.2")
            val nonExistingModule = Module("io.spine", "core-java")

            val dependencies = Dependencies(listOf(artifact, ivyDep))

            val found = dependencies.find(nonExistingModule)

            found shouldBe null
        }
    }

    @Nested
    inner class StoreToFile {

        @TempDir
        lateinit var tempDir: Path

        @Test
        fun `store dependencies to a file`() {
            // Create dependencies
            val artifact = MavenArtifact("io.spine.tools", "tool-base", "2.0.0")
            val ivyDep = IvyDependency("org.gradle", "wrapper", "7.4.2")
            val dependencies = Dependencies(listOf(artifact, ivyDep))

            // Create a file in the temp directory
            val file = tempDir.resolve("dependencies.txt").toFile()

            // Store dependencies to the file
            dependencies.store(file)

            // Read the file content
            val lines = Files.readAllLines(file.toPath())

            // Verify the content
            lines.let {
                it shouldHaveSize 2
                it[0] shouldBe artifact.toString()
                it[1] shouldBe ivyDep.toString()
            }
        }

        @Test
        fun `throw exception when storing to a directory`() {
            // Create dependencies
            val artifact = MavenArtifact("io.spine.tools", "tool-base", "2.0.0")
            val dependencies = Dependencies(listOf(artifact))

            // Try to store to a directory
            assertThrows<IllegalArgumentException> {
                dependencies.store(tempDir.toFile())
            }
        }
    }
}
