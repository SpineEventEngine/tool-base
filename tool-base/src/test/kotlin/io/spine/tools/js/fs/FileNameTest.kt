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
package io.spine.tools.js.fs

import com.google.common.testing.NullPointerTester
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Any
import io.spine.testing.Assertions.assertIllegalArgument
import org.junit.jupiter.api.Test

class `'FileName' should` {

    private val file = Any.getDescriptor().file
    private val fileName = FileName.from(file)

    @Test
    fun `handle 'null's`() {
        NullPointerTester().testAllPublicStaticMethods(FileName::class.java)
    }

    @Test
    fun `not accept names without extension`() {
        assertIllegalArgument { FileName.of("no-extension") }
    }

    @Test
    fun `replace 'proto' extension with predefined suffix`() {
        assertThat(fileName.value())
            .isEqualTo("google/protobuf/any_pb.js")
    }

    @Test
    fun `return path elements`() {
        val pathElements = fileName.pathElements()
        assertThat(pathElements)
            .containsExactly("google", "protobuf", "any_pb.js")
    }

    @Test
    fun `obtain relative path to source root dir`() {
        assertThat(fileName.pathToRoot()).startsWith("../../")
    }

    @Test
    fun `obtain path from source root dir`() {
        assertThat(fileName.pathFromRoot())
            .isEqualTo("./google/protobuf/any_pb.js")
    }
}
