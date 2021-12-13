/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import com.google.common.truth.Truth.assertThat
import io.spine.code.java.PackageName
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.jupiter.api.Test

class `'FsTypesExtensions' should` {

    @Test
    fun `convert Java package to a directory`() {
        val javaPackage = PackageName.of(String::class.java)

        assertThat(javaPackage.toDirectory())
            .isEqualTo(Paths.get("java/lang"))
    }

    @Test
    fun `obtain a source code file from a path and file name`() {
        val dir = Paths.get("some/dir/")
        val typeName = "SomethingBlue"
        val file = FileName.forType(typeName)
        val resolved = dir.resolve(file)
        assertThat(resolved.path().map(Path::toString))
            .containsExactly("some", "dir", "$typeName.java")
    }
}
