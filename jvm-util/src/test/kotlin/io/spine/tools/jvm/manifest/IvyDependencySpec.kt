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

package io.spine.tools.jvm.manifest

import io.kotest.matchers.shouldBe
import io.spine.testing.TestValues.randomString
import io.spine.tools.jvm.manifest.IvyDependency
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IvyDependencySpec {

    private lateinit var org: String
    private lateinit var name: String
    private lateinit var rev: String

    private lateinit var strForm: String

    @BeforeEach
    fun generateParts() {
        org = randomString()
        name = randomString()
        rev = randomString()
        strForm = "${IvyDependency.PREFIX} org=\"$org\" name=\"$name\" rev=\"$rev\""
    }

    @Test
    fun `provide string form with all components`() {
        val idep = IvyDependency(org, name, rev)

        idep.toString() shouldBe strForm
    }

    @Test
    fun `parse string representation`() {
        val idep = IvyDependency.parse(strForm)

        idep shouldBe IvyDependency(org, name, rev)
    }
}
