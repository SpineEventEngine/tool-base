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
import io.spine.tools.div
import io.spine.tools.fs.DirectoryName
import io.spine.tools.fs.DirectoryName.build
import java.nio.file.Path
import kotlin.io.path.div
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`DefaultJavaPaths` should")
internal class DefaultJavaPathsSpec {

    @Test
    fun `obtain 'build' dir`() {
        val paths = DefaultJavaPaths.at(projectPath)
        paths.buildRoot().path() shouldBe

                projectPath / build
    }

    @Test
    fun `obtain 'generated' dir`() {
        val paths = DefaultJavaPaths.at(projectPath)
        paths.generated().path() shouldBe

                projectPath / GENERATED_DIR
    }

    companion object {
        private val projectPath = Path.of("/test-path")
        private val GENERATED_DIR = DirectoryName.generated.value()
    }
}
