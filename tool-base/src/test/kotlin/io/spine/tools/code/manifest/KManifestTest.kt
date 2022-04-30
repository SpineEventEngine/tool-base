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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class `'KManifest' should` {

    @Test
    fun `load itself from resources`() {
        val manifest = KManifest.load(javaClass)

        assertThat(manifest.implementationVersion).isEqualTo("2.0.0-SNAPSHOT.92")
    }

    @Nested
    inner class `provide configuration via writer object for` {

        private lateinit var manifest: KManifest

        private lateinit var version: String

        @BeforeEach
        fun initManifest(@TempDir tmp: File) {
            version = randomString()

            val writer = KManifestWriter()
            writer.implementationVersion(version)

            val file = tmp.resolve("MANIFEST.MF")
            val output = FileOutputStream(file)
            output.use {
                writer.write(it)
            }

            val input = FileInputStream(file)
            input.use {
                manifest = KManifest.load(input)
            }
        }

        @Test
        fun implementationVersion() {
            assertThat(manifest.implementationVersion)
                .isEqualTo(version)
        }
    }
}
