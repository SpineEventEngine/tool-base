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

package io.spine.tools.java.code

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import javax.lang.model.element.Modifier
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("JavaPoet extensions should")
internal class PoetExtsSpec {

    @Test
    fun `build a method spec applying the builder action`() {
        val spec = methodSpec("doThing") {
            addModifiers(Modifier.PUBLIC)
        }

        spec.name shouldBe "doThing"
        spec.modifiers shouldContain Modifier.PUBLIC
    }

    @Test
    fun `build a class spec applying the builder action`() {
        val spec = classSpec("MyType") {
            addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        }

        spec.name shouldBe "MyType"
        spec.modifiers shouldContain Modifier.FINAL
    }

    @Test
    fun `build a constructor spec applying the builder action`() {
        val spec = constructorSpec {
            addModifiers(Modifier.PUBLIC)
        }

        spec.name shouldBe "<init>"
        spec.isConstructor shouldBe true
    }

    @Test
    fun `build a code block applying the builder action`() {
        val block = codeBlock {
            addStatement("int \$L = 1", "x")
        }

        block.toString() shouldContain "int x = 1;"
    }

    @Test
    fun `format a code block into a string`() {
        codeBlock("int \$L = 1;", "x") shouldBe "int x = 1;"
    }
}
