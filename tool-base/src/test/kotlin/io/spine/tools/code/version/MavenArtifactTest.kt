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

package io.spine.tools.code.version

import com.google.common.truth.Truth.assertThat
import io.spine.tools.code.version.MavenArtifact.Companion.PREFIX
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class `'MavenArtifact' should` {

    @Test
    fun `parse string form`() {
        val coordinates = "io.spine:spine-foo-bar:2.0.0-SNAPSHOT.91"
        val artifact = MavenArtifact.parse("maven:$coordinates")
        assertThat(artifact.coordinates).isEqualTo(coordinates)
    }

    @Test
    fun `require 'maven' prefix when parsing`() {
        val coordinates = "com.example:foo-bar:1.0"
        assertThrows<IllegalArgumentException> { MavenArtifact.parse(coordinates) }
        
        val parsed = MavenArtifact.parse(PREFIX + coordinates)
        assertThat(parsed.coordinates).isEqualTo(coordinates)
    }

    @Test
    fun `require 3 parts in Maven coordinates`() {
        assertThrows<IllegalArgumentException> {
            MavenArtifact("fiz")
        }
        assertThrows<IllegalArgumentException> {
            MavenArtifact("fiz:baz")
        }

        assertThrows<IllegalArgumentException> {
            MavenArtifact(":baz:bar")
        }

        assertThrows<IllegalArgumentException> {
            MavenArtifact("fiz::bar")
        }

        assertThrows<IllegalArgumentException> {
            MavenArtifact("fiz:baz:")
        }
    }

    @Test
    fun `expose properties of coordinates`() {
        val group = "io.spine"
        val name = "tool-base"
        val version = "2.0.0"

        val ma = MavenArtifact("$group:$name:$version")

        assertThat(ma.group).isEqualTo(group)
        assertThat(ma.name).isEqualTo(name)
        assertThat(ma.version).isEqualTo(version)
    }
}
