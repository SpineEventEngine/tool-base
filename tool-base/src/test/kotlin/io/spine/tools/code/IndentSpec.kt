/*
 * Copyright 2026, TeamDev. All rights reserved.
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

package io.spine.tools.code

import com.google.common.testing.EqualsTester
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Indent` should")
internal class IndentSpec {

    @Test
    fun `obtain an instance at the given level`() {
        val indent = Indent.of(2).at(3)

        indent.size() shouldBe 2
        indent.level() shouldBe 3
        indent.text() shouldBe " ".repeat(6)
    }

    @Test
    fun `reject a negative level`() {
        shouldThrow<IllegalArgumentException> { Indent.of(2).at(-1) }
    }

    @Test
    fun `shift to the right by one level`() {
        Indent.of(2).at(1).shiftedRight().level() shouldBe 2
    }

    @Test
    fun `shift to the left by one level`() {
        Indent.of(2).at(2).shiftedLeft().level() shouldBe 1
    }

    @Test
    fun `fail to shift to the left below zero`() {
        shouldThrow<IllegalStateException> { Indent.of(2).at(0).shiftedLeft() }
    }

    @Test
    fun `return itself when shifted by zero`() {
        val indent = Indent.of(2).at(1)
        indent.shifted(0) shouldBe indent
    }

    @Test
    fun `shift by a positive delta`() {
        Indent.of(2).at(1).shifted(2).level() shouldBe 3
    }

    @Test
    fun `fail to shift below zero`() {
        shouldThrow<IllegalArgumentException> { Indent.of(2).at(1).shifted(-2) }
    }

    @Test
    fun `support equality`() {
        EqualsTester()
            .addEqualityGroup(Indent.of(2).at(1), Indent.of(2).at(1))
            .addEqualityGroup(Indent.of(2).at(2))
            .addEqualityGroup(Indent.of(4).at(1))
            .testEquals()
    }
}
