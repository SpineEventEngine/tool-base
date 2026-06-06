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

package io.spine.tools.fs

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`ExternalModule` should")
internal class ExternalModuleSpec {

    @Test
    fun `expose its name`() {
        ExternalModule("foo", DirectoryPattern.listOf("foo/*")).name() shouldBe "foo"
    }

    @Test
    fun `tell whether it provides a file`() {
        val module = ExternalModule("m", DirectoryPattern.listOf("d"))

        module.provides(FileReference.of("./../../d/f.js")) shouldBe true
        module.provides(FileReference.of("./../../other/f.js")) shouldBe false
    }

    @Test
    fun `provide the Spine Users module`() {
        ExternalModule.spineUsers().name() shouldBe "spine-users"
    }

    @Test
    fun `provide the Spine Web module`() {
        ExternalModule.spineWeb().name() shouldBe "spine-web"
    }

    @Test
    fun `list the predefined modules`() {
        ExternalModule.predefinedModules() shouldHaveSize 2
    }

    @Test
    fun `not be equal to an object of a different type`() {
        @Suppress("EqualsBetweenInconvertibleTypes")
        ExternalModule.spineWeb().equals("spine-web") shouldBe false
    }
}
