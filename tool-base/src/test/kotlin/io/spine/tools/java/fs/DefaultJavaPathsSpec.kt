/*
 * Copyright 2023, TeamDev. All rights reserved.
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
package io.spine.tools.java.fs

import io.kotest.matchers.shouldBe
import io.spine.tools.code.SourceSetName
import io.spine.tools.fs.DirectoryName
import io.spine.tools.fs.DirectoryName.build
import io.spine.tools.fs.DirectoryName.generatedProto
import io.spine.tools.fs.DirectoryName.grpc
import io.spine.tools.fs.DirectoryName.spine
import io.spine.tools.resolve
import java.nio.file.Path
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`DefaultJavaPaths` should")
internal class DefaultJavaPathsSpec {

    @Test
    fun `obtain 'build' dir`() {
        val paths = DefaultJavaPaths.at(projectPath)
        paths.buildRoot().path() shouldBe projectPath.resolve(build)
    }

    @Test
    fun `obtain 'generated' dir`() {
        val paths = DefaultJavaPaths.at(projectPath)
        paths.generated().path() shouldBe projectPath.resolve(GENERATED_DIR)
    }

    @Test
    fun `obtain 'java' subdir in 'generated-proto' dir`() {
        val paths = DefaultJavaPaths.at(projectPath)

        paths.generatedProto().java(SourceSetName.main).path() shouldBe

                projectPath.resolve(build.value())
                    .resolve(generatedProto.value())
                    .resolve(MAIN_DIR)
                    .resolve(JAVA_DIR)
    }

    @Test
    fun `obtain 'spine' subdir in 'generated' dir`() {
        val paths = DefaultJavaPaths.at(projectPath)

        paths.generated().spine(SourceSetName.test).path() shouldBe

                projectPath.resolve(GENERATED_DIR)
                    .resolve(TEST_DIR)
                    .resolve(spine.value())
    }

    @Test
    @DisplayName("obtain `grpc` subdir in `generated-proto` dir")
    fun generatedGrpc() {
        val paths = DefaultJavaPaths.at(projectPath)

        paths.generatedProto().grpc(SourceSetName.test).path() shouldBe
                projectPath.resolve(build)
                    .resolve(generatedProto)
                    .resolve(TEST_DIR)
                    .resolve(grpc)
    }

    companion object {
        private val projectPath = Path.of("/test-path")
        private val JAVA_DIR = DirectoryName.java.value()
        private val GENERATED_DIR = DirectoryName.generated.value()
        private const val TEST_DIR = "test"
        private const val MAIN_DIR = "main"
    }
}
