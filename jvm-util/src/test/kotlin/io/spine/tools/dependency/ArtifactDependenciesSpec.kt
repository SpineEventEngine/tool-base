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

package io.spine.tools.dependency

import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("`ArtifactDependencies` should")
internal class ArtifactDependenciesSpec {

    @Test
    fun `return artifact identifier in toString`() {
        val artifact = MavenArtifact("io.spine.tools", "tool-base", "2.0.0")
        val dependencies = Dependencies(emptyList())

        val artifactDependencies = ArtifactDependencies(artifact, dependencies)

        artifactDependencies.toString() shouldBe artifact.toString()
    }

    @Nested
    inner class StoreAndLoad {

        @TempDir
        lateinit var tempDir: Path

        @Test
        fun `store and load with no dependencies`() {
            val artifact = MavenArtifact("io.spine.tools", "tool-base", "2.0.0")
            val dependencies = Dependencies(emptyList())
            val artifactDependencies = ArtifactDependencies(artifact, dependencies)

            val file = tempDir.resolve("artifact-dependencies.txt").toFile()

            artifactDependencies.store(file)

            val loaded = ArtifactDependencies.load(file)

            loaded.artifact shouldBe artifact
            loaded.dependencies.list shouldHaveSize 0
        }

        @Test
        fun `store and load with dependencies`() {
            val artifact = MavenArtifact("io.spine.tools", "tool-base", "2.0.0")
            val dep1 = MavenArtifact("io.spine", "core-java", "2.0.1")
            val dep2 = IvyDependency("org.gradle", "wrapper", "7.4.2")
            val dependencies = Dependencies(listOf(dep1, dep2))
            val artifactDependencies = ArtifactDependencies(artifact, dependencies)

            val file = tempDir.resolve("artifact-dependencies.txt").toFile()

            artifactDependencies.store(file)

            val loaded = ArtifactDependencies.load(file)

            loaded.artifact shouldBe artifact
            loaded.dependencies.list shouldHaveSize 2
            loaded.dependencies.list[0] shouldBe dep1
            loaded.dependencies.list[1] shouldBe dep2
        }

        @Test
        fun `verify file content`() {
            val artifact = MavenArtifact("io.spine.tools", "tool-base", "2.0.0")
            val dep1 = MavenArtifact("io.spine", "core-java", "2.0.1")
            val dep2 = IvyDependency("org.gradle", "wrapper", "7.4.2")
            val dependencies = Dependencies(listOf(dep1, dep2))
            val artifactDependencies = ArtifactDependencies(artifact, dependencies)

            val file = tempDir.resolve("artifact-dependencies.txt").toFile()

            artifactDependencies.store(file)

            val lines = Files.readAllLines(file.toPath())

            lines shouldHaveSize 3
            lines[0] shouldBe artifact.toString()
            lines[1] shouldBe dep1.toString()
            lines[2] shouldBe dep2.toString()
        }
    }

    @Nested
    @DisplayName("throw `IllegalArgumentException` when")
    inner class ThrowExceptions {

        @TempDir
        lateinit var tempDir: Path

        @Test
        fun `storing to a directory`() {
            val artifact = MavenArtifact("io.spine.tools", "tool-base", "2.0.0")
            val dependencies = Dependencies(emptyList())
            val artifactDependencies = ArtifactDependencies(artifact, dependencies)

            assertThrows<IllegalArgumentException> {
                artifactDependencies.store(tempDir.toFile())
            }
        }

        @Test
        fun `loading from a non-existent file`() {
            val file = tempDir.resolve("non-existent.txt").toFile()

            assertThrows<IllegalArgumentException> {
                ArtifactDependencies.load(file)
            }
        }

        @Test
        fun `loading from a directory`() {
            assertThrows<IllegalArgumentException> {
                ArtifactDependencies.load(tempDir.toFile())
            }
        }

        @Test
        fun `loading from an empty file`() {
            val file = tempDir.resolve("empty.txt").toFile()
            file.createNewFile()

            assertThrows<IllegalArgumentException> {
                ArtifactDependencies.load(file)
            }
        }

        @Test
        fun `first line is not a Maven artifact`() {
            val file = tempDir.resolve("invalid.txt").toFile()
            Files.write(file.toPath(), listOf("invalid content"))

            assertThrows<IllegalArgumentException> {
                ArtifactDependencies.load(file)
            }
        }
    }
}
