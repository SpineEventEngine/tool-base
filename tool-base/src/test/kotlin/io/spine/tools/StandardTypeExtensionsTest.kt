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

package io.spine.tools

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.function.Supplier
import org.junit.jupiter.api.Test

class `'StandardTypeExtensions' should` {

    @Test
    fun `provide title case version of 'String'`() {
        assertThat("foo".titlecaseFirstChar())
            .isEqualTo("Foo")
        assertThat("Bar".titlecaseFirstChar())
            .isEqualTo("Bar")
    }

    @Test
    fun `convert a 'String' 'Supplier' to absolute file`() {
        val sup: Supplier<String> = Supplier { "." }

        assertThat(sup.toAbsoluteFile().isAbsolute)
            .isTrue()
    }

    @Test
    fun `tell if a file is a Protobuf source code file`() {
        assertThat(File("mycode.proto").isProtoSource())
            .isTrue()
        assertThat(File("util.java").isProtoSource())
            .isFalse()
    }
}
