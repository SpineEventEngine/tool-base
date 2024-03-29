/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.tools.java

import io.kotest.matchers.shouldBe
import io.spine.string.ti
import java.io.File.pathSeparator
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Kotlin extension for `Classpath` should")
internal class ClasspathExtsSpec {

    @Test
    fun `print classpath string`() {
        val items = listOf(
            "/some/path/fiz.jar",
            "/another/path/buz.jar"
        )
        val cp = items.joinToString(pathSeparator)

        val classpath = parseClasspath(cp)

        classpath.printItems() shouldBe
        """
            ${items[0]}$pathSeparator
            ${items[1]}
        """.ti()
    }

    @Test
    fun `filter JAR files`() {
        val items = listOf(
            "/some/path/fiz.jar",
            "/another/path/buz.jar",
            "/yet/another/path/baz.class"
        )
        val cp = items.joinToString(pathSeparator)

        val classpath = parseClasspath(cp)

        classpath.jars() shouldBe listOf(items[0], items[1])
    }

    @Test
    fun `provide shortcut for obtaining all items`() {
        val items = listOf(
            "/some/path/foo.jar",
            "/another/path/biz.jar",
            "/yet/another/path/bar.class"
        )
        val cp = items.joinToString(pathSeparator)

        val classpath = parseClasspath(cp)

        classpath.items() shouldBe items
    }
}
