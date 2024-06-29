/*
 * Copyright 2024, TeamDev. All rights reserved.
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

package io.spine.tools.gradle

import io.kotest.matchers.collections.shouldContainInOrder
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`Ordered` should")
internal class OrderedSpec {

    private val project = ProjectBuilder.builder().build()

    @Test
    fun `be created using Kotlin class`() {
        val m = Ordered(project, String::class)
        val expected = listOf("apple", "orange", "banana")
        m.set(expected)
        m.get() shouldContainInOrder expected
    }

    @Test
    fun `be created using Java class`() {
        val m = Ordered(project, String::class.java)
        val expected = listOf("Espresso", "Breve", "Cappuccino", "Latte")
        m.set(expected)
        m.get() shouldContainInOrder expected
    }

    @Test
    fun `run transformation`() {
        val m = Ordered(project, Int::class)
        m.set(listOf(1, 2, 3))
        m.transform { it * it } shouldContainInOrder listOf(1, 4, 9)
    }
}
