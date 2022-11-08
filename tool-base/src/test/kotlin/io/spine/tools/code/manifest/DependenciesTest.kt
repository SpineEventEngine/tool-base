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

package io.spine.tools.code.manifest

import com.google.common.truth.Truth.assertThat
import io.spine.testing.TestValues.randomString
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class `'Dependencies' should` {

    @Test
    fun `escapes quotes in string form`() {
        val org = "io.spine"
        val name = "tool-base"
        val rev = "2.0.0"

        val ivyDep = IvyDependency(org, name, rev)
        val escaped = ivyDep.toString().replace("\"", "\\\"")

        val deps = Dependencies(listOf(ivyDep))
        val depsStr = deps.toString()

        assertThat(depsStr).isEqualTo("\"${escaped}\"")
    }

    /**
     * Imaginary case of something unusual in the version component, including comma and
     * space characters to test parsing against spaces and commas inside coordinates.
     */
    private fun unusualVersion(): String = randomString() + ", and then, some"

    @Nested
    inner class Parse {

        @Test
        fun `empty string`() {
            assertThat(Dependencies.parse("").list).isEmpty()
        }

        @Test
        fun `one Maven dependency`() {
            val group = randomString()
            val name = randomString()
            val version = unusualVersion()
            val ma = MavenArtifact("$group:$name:$version")
            val depStr = "\"$ma\""

            val deps = Dependencies.parse(depStr)

            assertThat(deps.list).hasSize(1)
            assertThat(deps.list[0]).isEqualTo(ma)
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

            assertThat(parsed).isEqualTo(original)
        }
    }
}
