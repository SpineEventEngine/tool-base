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

package io.spine.tools.version

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Version should")
class VersionSpec {

    @Nested
    @DisplayName("construct with")
    inner class Construction {

        @Test
        fun `major, minor, and patch versions`() {
            val version = Version(1, 2, 3)
            version.major shouldBe 1
            version.minor shouldBe 2
            version.patch shouldBe 3
            version.preRelease shouldBe null
            version.buildMetadata shouldBe null
        }

        @Test
        fun `pre-release identifier`() {
            val version = Version(1, 2, 3, "alpha")
            version.major shouldBe 1
            version.minor shouldBe 2
            version.patch shouldBe 3
            version.preRelease shouldBe "alpha"
            version.buildMetadata shouldBe null
        }

        @Test
        fun `build metadata`() {
            val version = Version(1, 2, 3, null, "build.123")
            version.major shouldBe 1
            version.minor shouldBe 2
            version.patch shouldBe 3
            version.preRelease shouldBe null
            version.buildMetadata shouldBe "build.123"
        }

        @Test
        fun `pre-release and build metadata`() {
            val version = Version(1, 2, 3, "beta", "build.456")
            version.major shouldBe 1
            version.minor shouldBe 2
            version.patch shouldBe 3
            version.preRelease shouldBe "beta"
            version.buildMetadata shouldBe "build.456"
        }
    }

    @Nested
    @DisplayName("calculate value as string with")
    inner class ValueCalculation {

        @Test
        fun `major, minor, and patch versions`() {
            val version = Version(1, 2, 3)
            version.value shouldBe "1.2.3"
        }

        @Test
        fun `pre-release identifier`() {
            val version = Version(1, 2, 3, "alpha")
            version.value shouldBe "1.2.3-alpha"
        }

        @Test
        fun `build metadata`() {
            val version = Version(1, 2, 3, null, "build.123")
            version.value shouldBe "1.2.3+build.123"
        }

        @Test
        fun `pre-release and build metadata`() {
            val version = Version(1, 2, 3, "beta", "build.456")
            version.value shouldBe "1.2.3-beta+build.456"
        }
    }

    @Nested
    @DisplayName("parse from string")
    inner class Parsing {

        @Test
        fun `simple version`() {
            val version = Version.parse("1.2.3")
            version.major shouldBe 1
            version.minor shouldBe 2
            version.patch shouldBe 3
            version.preRelease shouldBe null
            version.buildMetadata shouldBe null
        }

        @Test
        fun `version with pre-release`() {
            val version = Version.parse("1.2.3-alpha")
            version.major shouldBe 1
            version.minor shouldBe 2
            version.patch shouldBe 3
            version.preRelease shouldBe "alpha"
            version.buildMetadata shouldBe null
        }

        @Test
        fun `version with build metadata`() {
            val version = Version.parse("1.2.3+build.123")
            version.major shouldBe 1
            version.minor shouldBe 2
            version.patch shouldBe 3
            version.preRelease shouldBe null
            version.buildMetadata shouldBe "build.123"
        }

        @Test
        fun `version with pre-release and build metadata`() {
            val version = Version.parse("1.2.3-beta+build.456")
            version.major shouldBe 1
            version.minor shouldBe 2
            version.patch shouldBe 3
            version.preRelease shouldBe "beta"
            version.buildMetadata shouldBe "build.456"
        }

        @Test
        fun `complex pre-release identifiers`() {
            val version = Version.parse("1.2.3-alpha.1.beta.2")
            version.major shouldBe 1
            version.minor shouldBe 2
            version.patch shouldBe 3
            version.preRelease shouldBe "alpha.1.beta.2"
            version.buildMetadata shouldBe null
        }

        @Test
        fun `throw on invalid version format`() {
            shouldThrow<IllegalStateException> {
                Version.parse("invalid")
            }

            shouldThrow<IllegalStateException> {
                Version.parse("1.2")
            }

            shouldThrow<IllegalStateException> {
                Version.parse("1.a.3")
            }
        }
    }

    @Nested
    @DisplayName("compare versions")
    inner class Comparison {

        @Test
        fun `by major version`() {
            Version(2, 0, 0) shouldBe Version(2, 0, 0)
            Version(1, 0, 0) shouldBe Version(1, 0, 0)

            (Version(2, 0, 0) > Version(1, 0, 0)) shouldBe true
            (Version(1, 0, 0) < Version(2, 0, 0)) shouldBe true
        }

        @Test
        fun `by minor version when major is equal`() {
            Version(1, 2, 0) shouldBe Version(1, 2, 0)

            (Version(1, 2, 0) > Version(1, 1, 0)) shouldBe true
            (Version(1, 1, 0) < Version(1, 2, 0)) shouldBe true
        }

        @Test
        fun `by patch version when major and minor are equal`() {
            Version(1, 2, 3) shouldBe Version(1, 2, 3)

            (Version(1, 2, 3) > Version(1, 2, 2)) shouldBe true
            (Version(1, 2, 2) < Version(1, 2, 3)) shouldBe true
        }

        @Test
        fun `pre-release has lower precedence than normal version`() {
            (Version(1, 2, 3) > Version(1, 2, 3, "alpha")) shouldBe true
            (Version(1, 2, 3, "alpha") < Version(1, 2, 3)) shouldBe true
        }

        @Test
        fun `pre-release identifiers by numeric value`() {
            (Version(1, 2, 3, "alpha.2") > Version(1, 2, 3, "alpha.1")) shouldBe true
            (Version(1, 2, 3, "alpha.1") < Version(1, 2, 3, "alpha.2")) shouldBe true
        }

        @Test
        fun `pre-release identifiers lexically`() {
            (Version(1, 2, 3, "beta") > Version(1, 2, 3, "alpha")) shouldBe true
            (Version(1, 2, 3, "alpha") < Version(1, 2, 3, "beta")) shouldBe true
        }

        @Test
        fun `numeric identifiers have lower precedence than non-numeric`() {
            (Version(1, 2, 3, "alpha.beta") > Version(1, 2, 3, "alpha.1")) shouldBe true
            (Version(1, 2, 3, "alpha.1") < Version(1, 2, 3, "alpha.beta")) shouldBe true
        }

        @Test
        fun `longer set of pre-release identifiers has higher precedence`() {
            (Version(1, 2, 3, "alpha.beta.1") > Version(1, 2, 3, "alpha.beta")) shouldBe true
            (Version(1, 2, 3, "alpha.beta") < Version(1, 2, 3, "alpha.beta.1")) shouldBe true
        }

        @Test
        fun `build metadata does not affect precedence`() {
            // Use compareTo instead of equality since data class equality includes all fields.
            Version(1, 2, 3, null, "build.1").compareTo(
                Version(1, 2, 3, null, "build.2")
            ) shouldBe 0
            Version(1, 2, 3, "alpha", "build.1").compareTo(
                Version(1, 2, 3, "alpha", "build.2")
            ) shouldBe 0
        }
    }
}
