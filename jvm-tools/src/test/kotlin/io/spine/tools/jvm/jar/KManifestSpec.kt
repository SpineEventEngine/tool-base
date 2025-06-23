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

package io.spine.tools.jvm.jar

import com.google.common.truth.Truth.assertWithMessage
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.beEmpty
import java.nio.file.Files.createFile
import java.nio.file.Path
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`KManifest` should")
class KManifestSpec {

    @Test
    fun `load itself from resources`() {
        val cls = KManifest::class.java
        val manifest = KManifest.load(cls)

        val nl = System.lineSeparator()
        val visibleManifests = manifestsVisibleTo(cls).joinToString(nl)
        assertWithMessage(
            "Unable to load correct manifest for the class `${cls.name}`."
                    + " Visible manifests are: $nl"
                    + visibleManifests
        )
            .that(manifest.implementationTitle)
            .isEqualTo("io.spine.tools:jvm-tools")

        manifest.implementationVersion shouldNot beEmpty()
    }

    @Test
    fun `print its content to string as if it were a resource file`(@TempDir tmpDir: Path) {
        val cls = KManifest::class.java
        val manifest = KManifest.load(cls)

        val tmpFile = tmpDir.resolve("MANIFEST.MF")
        createFile(tmpFile)
        val stream = tmpFile.outputStream()
        stream.use {
            manifest.impl.write(it)
        }

        manifest.toString() shouldBe tmpFile.readText()
    }
}
