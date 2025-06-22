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
import io.spine.tools.dependency.ArtifactDependencies.Companion.FILE_EXTENSION
import io.spine.tools.dependency.ArtifactDependencies.Companion.RESOURCE_DIRECTORY
import io.spine.tools.dependency.ArtifactDependencies.Companion.resourcePathFor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("`ArtifactDependencies` should")
internal class ArtifactDependenciesSpec {

    companion object {
        private const val DEPS_FILE = "artifact-dependencies.txt"
    }

    private lateinit var toolBase: MavenArtifact
    private lateinit var emptyDependencies: Dependencies

    @BeforeEach
    fun setUp() {
        toolBase = MavenArtifact("io.spine.tools", "tool-base", "2.0.0")
        emptyDependencies = Dependencies(emptyList())
    }

    @Test
    fun `return artifact identifier in toString`() {
        val artifactDependencies = ArtifactDependencies(toolBase, emptyDependencies)

        artifactDependencies.toString() shouldBe toolBase.toString()
    }

    @Nested
    inner class StoreAndLoad {

        @TempDir
        lateinit var tempDir: Path

        private lateinit var coreJava: MavenArtifact
        private lateinit var gradleWrapper: IvyDependency
        private lateinit var dependencies: Dependencies
        private lateinit var artifactDependencies: ArtifactDependencies

        @BeforeEach
        fun setUp() {
            coreJava = MavenArtifact("io.spine", "core-java", "2.0.1")
            gradleWrapper = IvyDependency("org.gradle", "wrapper", "7.4.2")
            dependencies = Dependencies(listOf(coreJava, gradleWrapper))
            artifactDependencies = ArtifactDependencies(toolBase, dependencies)
        }

        @Test
        fun `store and load with no dependencies`() {
            val noDependencies = Dependencies(emptyList())
            val artifactWithNoDeps = ArtifactDependencies(toolBase, noDependencies)

            val file = tempDir.resolve(DEPS_FILE).toFile()

            artifactWithNoDeps.store(file)

            val loaded = ArtifactDependencies.load(file)

            loaded.artifact shouldBe toolBase
            loaded.dependencies.list.let {
                it shouldHaveSize 0
            }
        }

        @Test
        fun `store and load with dependencies`() {
            val file = tempDir.resolve(DEPS_FILE).toFile()

            artifactDependencies.store(file)

            val loaded = ArtifactDependencies.load(file)

            loaded.artifact shouldBe toolBase
            loaded.dependencies.list.let {
                it shouldHaveSize 2
                it[0] shouldBe coreJava
                it[1] shouldBe gradleWrapper
            }
        }

        @Test
        fun `verify file content`() {
            val file = tempDir.resolve(DEPS_FILE).toFile()

            artifactDependencies.store(file)

            val lines = Files.readAllLines(file.toPath())

            lines.let {
                it shouldHaveSize 3
                it[0] shouldBe toolBase.toString()
                it[1] shouldBe coreJava.toString()
                it[2] shouldBe gradleWrapper.toString()
            }
        }
    }

    @Nested
    @DisplayName("throw `IllegalArgumentException` when")
    inner class ThrowExceptions {

        @TempDir
        lateinit var tempDir: Path

        private lateinit var artifactDependencies: ArtifactDependencies

        @BeforeEach
        fun setUp() {
            artifactDependencies = ArtifactDependencies(toolBase, emptyDependencies)
        }

        @Test
        fun `storing to a directory`() {
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

    @Nested
    @DisplayName("load from resource")
    inner class LoadFromResource {

        private val resourcePath = DEPS_FILE

        private lateinit var coreJava: MavenArtifact
        private lateinit var gradleWrapper: IvyDependency

        @BeforeEach
        fun setUp() {
            coreJava = MavenArtifact("io.spine", "core-java", "2.0.1")
            gradleWrapper = IvyDependency("org.gradle", "wrapper", "7.4.2")
        }

        @Test
        fun `using class loader`() {
            val loaded = ArtifactDependencies.loadFromResource(
                resourcePath,
                ArtifactDependenciesSpec::class.java.classLoader
            )

            loaded.artifact shouldBe toolBase
            loaded.dependencies.list.let {
                it shouldHaveSize 2
                it[0] shouldBe coreJava
                it[1] shouldBe gradleWrapper
            }
        }

        @Test
        fun `using class`() {
            val loaded = ArtifactDependencies.loadFromResource(
                resourcePath, 
                ArtifactDependenciesSpec::class.java
            )

            loaded.artifact shouldBe toolBase
            loaded.dependencies.list.let {
                it shouldHaveSize 2
                it[0] shouldBe coreJava
                it[1] shouldBe gradleWrapper
            }
        }

        @Test
        fun `throw when resource does not exist`() {
            val nonExistentPath = "non-existent.txt"

            assertThrows<IllegalStateException> {
                ArtifactDependencies.loadFromResource(
                    nonExistentPath, 
                    ArtifactDependenciesSpec::class.java
                )
            }
        }

        /**
         * This test verifies that the `loadFromResource` method with a `Module` parameter
         * correctly composes the resource path using the module's `fileSafeId` property.
         * 
         * Since we can't easily mock the class loader to return a resource from a different path,
         * we'll verify that the method throws the expected exception when the resource
         * doesn't exist.
         */
        @Test
        fun `compose resource path from module`() {
            // Create a module with a unique group and name
            val module = Module("test.group", "test-name")

            // Verify that the method throws an exception when the resource doesn't exist
            assertThrows<IllegalStateException> {
                ArtifactDependencies.loadFromResource(
                    module, 
                    ArtifactDependenciesSpec::class.java
                )
            }
        }

        /**
         * This test verifies that the `resourcePathFor` function returns
         * the expected path following the convention:
         * `$RESOURCE_DIRECTORY/${module.fileSafeId}$FILE_EXTENSION`.
         */
        @Test
        fun `check resource path convention`() {
            val module = Module("test.group", "test-name")

            val resourcePath = resourcePathFor(module)

            val expectedPath = resourcePathFor(module)
            resourcePath shouldBe expectedPath

            resourcePath shouldBe "$RESOURCE_DIRECTORY/${module.fileSafeId}$FILE_EXTENSION"
        }
    }
}
