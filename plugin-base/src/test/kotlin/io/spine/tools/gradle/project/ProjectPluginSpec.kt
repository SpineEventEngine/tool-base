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

package io.spine.tools.gradle.project

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.spine.string.qualifiedClassName
import io.spine.tools.gradle.DslSpec
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`ProjectPlugin` should")
class ProjectPluginSpec {

    private lateinit var project: Project
    private lateinit var dslSpec: DslSpec<*>

    @BeforeEach
    fun setUp() {
        project = ProjectBuilder.builder()
            .withName("stub-project")
            .build()
        dslSpec = StubExtension.dslSpec
    }

    @Test
    fun `remember the project on 'apply'`() {
        val plugin = StubPlugin<Unit>(null)

        plugin.apply(project)

        plugin.project() shouldBe project
    }

    @Test
    fun `support working without an extension`() {
        val plugin = StubPlugin<Unit>(null)

        plugin.hasExtension shouldBe false

        plugin.apply(project)

        plugin.doCreateExtension() shouldBe null
    }

    @Test
    fun `create an extension when requested after the plugin is applied`() {
        val plugin = StubPlugin(dslSpec)

        plugin.hasExtension shouldBe true
        plugin.apply(project)

        plugin.doCreateExtension() shouldNotBe null
    }

    @Test
    fun `throw 'ISE' when trying to create an extension before 'apply'`() {
        val plugin = StubPlugin(dslSpec)
        val e = assertThrows<IllegalStateException> {
            plugin.doCreateExtension()
        }
        e.message shouldContain plugin.qualifiedClassName
    }

    @Test
    fun `return the already created extension when 'createExtension' called repeatedly`() {
        val plugin = StubPlugin(dslSpec)

        plugin.apply(project)
        
        val ext = plugin.doCreateExtension()
        plugin.doCreateExtension() shouldBe ext
    }
}

abstract class StubExtension {

    companion object {

        /**
         * The recommended way for encapsulating a `DslSpec` instance.
         */
        val dslSpec = DslSpec("stub", StubExtension::class)
    }
}

/**
 * This stub plugin class accepts a [DslSpec] instance as a parameter for
 * the needs of test that check plugins that work without extensions.
 *
 * The real plugin classes are expected to pass a [DslSpec] to a constructor
 * of the inherited class.
 */
private class StubPlugin<E: Any>(spec: DslSpec<E>?) : ProjectPlugin<E>(spec) {

    override val dslParent: ExtensionAware?
        get() = project as ExtensionAware

    /**
     * Opens access to the `protected` [project] property.
     */
    fun project() = project

    /**
     * Open access to the `protected` [createExtension] function.
     */
    fun doCreateExtension() = createExtension()
}
