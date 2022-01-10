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

package io.spine.tools.gragle.testing

import com.google.common.testing.NullPointerTester
import com.google.common.truth.Truth.assertThat
import io.spine.tools.gradle.task.JavaTaskName.Companion.compileJava
import io.spine.tools.gradle.testing.GradleProject
import io.spine.tools.gradle.testing.GradleProjectSetup
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

class `'GradleProjectSetup' should` {

    private lateinit var projectDir: File
    private lateinit var setup: GradleProjectSetup

    @BeforeEach
    fun setup(@TempDir projectDir: File) {
        this.projectDir = projectDir
        setup = GradleProject.setupAt(projectDir)
    }

    @Test
    fun `not accept 'null' arguments`() {
        NullPointerTester().testAllPublicInstanceMethods(setup)
    }

    @Test
    fun `provide project directory`() {
        assertThat(setup.projectDir)
            .isEqualTo(projectDir)
    }

    @Test
    fun `have 'debug' turned off by default`() {
        assertThat(setup.debug)
            .isFalse()
    }

    @Test
    fun `do not require 'buildSrc' directory by default`() {
        assertThat(setup.buildSrcCopy)
            .isNull()
    }

    @Test
    fun `enable debug mode`() {
        setup.enableRunnerDebug()
        assertThat(setup.debug)
            .isTrue()
    }

    @Test
    fun `add plugin under test classpath`() {
        setup.withPluginClasspath()
        assertThat(setup.addPluginUnderTestClasspath)
            .isTrue()
    }

    @Nested
    inner class `not allow setting both 'debug' mode and environment variables` {

        @Test
        fun `when 'debug' already set`() {
            setup.enableRunnerDebug()
            assertThrows<IllegalStateException> {
                setup.withEnvironment(mapOf( "foo" to "bar"))
            }
        }

        @Test
        fun `when environment vars already set`() {
            setup.withEnvironment(mapOf("fiz" to "baz"))
            assertThrows<IllegalStateException> {
                setup.enableRunnerDebug()
            }
        }
    }

    @Test
    fun `allow passing properties`() {
        val name = "cowboy"
        val value = "bebop"
        setup.withProperty(name, value)
        
        val assertArgs = assertCommandLineArgs()
        assertArgs.contains(name)
        assertArgs.contains(value)
    }

    private fun commandLineArgs() = setup.arguments.forTask(compileJava).toString()
    private fun assertCommandLineArgs() = assertThat(commandLineArgs())

    @Nested
    inner class `turn debug logging level` {

        private val debugOption = "--debug"

        @Test
        fun `having it off by default`() {
            assertCommandLineArgs().doesNotContain(debugOption)
        }

        @Test
        fun `when instructed`() {
            setup.debugLogging()
            assertCommandLineArgs().contains(debugOption)
        }
    }

    @Nested
    inner class `pass custom command line options` {

        private val options = listOf(
            "--console=plain",
            "-Dorg.gradle.daemon.debug=true"
        )

        @Test
        fun `passed as 'vararg'`() {
            setup.withOptions(options[0], options[1])
            assertCommandLineArgs().contains(options[0])
            assertCommandLineArgs().contains(options[1])
        }

        @Test
        fun `pass as 'Iterable'`() {
            setup.withOptions(options)
            options.forEach { assertCommandLineArgs().contains(it) }
        }
    }
}
