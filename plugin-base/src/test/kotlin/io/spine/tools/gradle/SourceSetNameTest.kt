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

package io.spine.tools.gradle

import com.google.common.truth.Truth.assertThat
import io.spine.tools.gradle.SourceSetName.Companion.main
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class `'SourceSetName' should` {

    @Test
    fun `reject empty values`() {
        assertThrows<IllegalArgumentException> { SourceSetName("") }
    }

    @Test
    fun `reject blank values`() {
        assertThrows<IllegalArgumentException> { SourceSetName(" ") }
    }

    @Test
    fun `expose the value`() {
        assertThat(main.value)
            .isEqualTo(MAIN_SOURCE_SET_NAME)
    }

    @Nested
    inner class `provide infix form` {

        @Test
        fun `of 'main' as empty string`() {
            assertThat(main.toInfix())
                .isEmpty()
        }

        @Test
        fun `as value in 'TitleCase'`() {
            val customName = "customName"
            assertThat(SourceSetName(customName).toInfix())
                .isEqualTo(customName.titlecaseFirstChar())
        }
    }

    @Nested
    inner class `provide prefix form` {

        @Test
        fun `of 'main' as empty string`() {
            assertThat(main.toPrefix())
                .isEmpty()
        }

        @Test
        fun `as value`() {
            val mySourceSetName = "mySourceSetName"
            assertThat(SourceSetName(mySourceSetName).toPrefix())
                .isEqualTo(mySourceSetName)
        }
    }
}
