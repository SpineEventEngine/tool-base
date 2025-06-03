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

package io.spine.tools.gradle

import com.google.common.testing.EqualsTester
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.string.simply
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`DslSpec` should")
internal class DslSpecSpec {

    private lateinit var project: Project
    private lateinit var dslSpec: DslSpec<*>

    @BeforeEach
    fun createProject() {
        project = ProjectBuilder.builder().build()
        dslSpec = DslSpec(StubExtension.NAME, StubExtension::class)
    }

    @Test
    fun `create a new instance in the given 'ExtensionAware' instance`() {
        dslSpec.findOrCreateIn(project)
        project.extensions.findByName(StubExtension.NAME) shouldNotBe null
    }

    @Test
    fun `obtain already created instance of an extension 'ExtensionAware' instance`() {
        val ext = dslSpec.findOrCreateIn(project)
        dslSpec.findOrCreateIn(project) shouldBe ext
    }

    @Test
    fun `provide 'equals' and 'hashCode'`() {
        val spec1 = DslSpec(StubExtension.NAME, StubExtension::class)
        val spec2 = DslSpec(StubExtension.NAME, StubExtension::class)
        val differentNameSpec = DslSpec("different", StubExtension::class)
        val differentClassSpec = DslSpec(
            DifferentStubExtension.NAME,
            DifferentStubExtension::class
        )

        EqualsTester()
            .addEqualityGroup(spec1, spec2)
            .addEqualityGroup(differentNameSpec)
            .addEqualityGroup(differentClassSpec)
            .testEquals()
    }

    @Test
    fun `implement 'toString' for diagnostics`() {
        val cls = StubExtension::class
        val spec = DslSpec(StubExtension.NAME, cls)
        val expectedString =
            "${simply<DslSpec<*>>()}(name='${StubExtension.NAME}', extensionClass=$cls)"
        spec.toString() shouldBe expectedString
    }
}

@Suppress("UtilityClassWithPublicConstructor") // Make `detekt` happy.
abstract class StubExtension {
    companion object {
        const val NAME = "stub"
    }
}

@Suppress("UtilityClassWithPublicConstructor") // Make `detekt` happy.
abstract class DifferentStubExtension {
    companion object {
        const val NAME = "differentStub"
    }
}
