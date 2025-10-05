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

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`LazyMeta` should")
internal class LazyMetaSpec {

    /**
     * An implementation that loads meta from test resources at
     * `META-INF/io.spine/test.group_test-artifact.meta`.
     */
    private object TestMeta : LazyMeta(Module("test.group", "test-artifact"))

    @Nested
    inner class `resolve dependency from meta` {
        @Test
        fun `return Maven artifact for a stored dependency`() {
            val depModule = Module(GRPC_GROUP, "grpc-kotlin-stub")

            val artifact = TestMeta.dependency(depModule)

            artifact shouldBe MavenArtifact(GRPC_GROUP, "grpc-kotlin-stub", "1.2.3")
        }

        @Test
        fun `throw if dependency is not found`() {
            val missing = Module("com.example", "missing-artifact")

            val ex = assertThrows<IllegalStateException> {
                TestMeta.dependency(missing)
            }
            // Message contains the missing module and mentions meta.
            ex.message shouldContain missing.toString()
        }

    }

    @Nested
    inner class `load meta lazily` {
        @Test
        fun `throw if meta resource is missing`() {
            // This module has no corresponding test resource under `META-INF/io.spine`.
            object : LazyMeta(Module("absent.group", "absent-artifact")) {}.apply {
                assertThrows<IllegalStateException> {
                    // Attempting to resolve any dependency should trigger meta loading.
                    dependency(Module(GRPC_GROUP, "grpc-kotlin-stub"))
                }
            }
        }
    }

    @Nested
    inner class `support 'LazyDependency' wrapper` {
        @Test
        fun `resolve artifact via LazyDependency`() {
            val module = Module(GRPC_GROUP, "protoc-gen-grpc-java")
            val lazy = LazyDependency(TestMeta, module)

            val artifact = lazy.artifact

            artifact shouldBe MavenArtifact(GRPC_GROUP, "protoc-gen-grpc-java", "1.2.3")
        }
    }

    companion object {
        private const val GRPC_GROUP = "io.grpc"
    }
}
