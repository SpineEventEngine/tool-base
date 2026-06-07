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

package io.spine.tools.gradle.testing

import com.google.common.testing.NullPointerTester
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.spine.tools.gradle.task.JavaTaskName.Companion.compileJava
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Predicate
import org.gradle.api.logging.LogLevel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

@DisplayName("`GradleProjectSetup` should")
class GradleProjectSetupSpec {

    private lateinit var projectDir: File
    private lateinit var setup: GradleProjectSetup

    private val commandLineArgs: String
        get() = setup.arguments.forTask(compileJava).toString()

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
        setup.projectDir shouldBe projectDir
    }

    @Test
    fun `have 'debug' turned off by default`() {
        setup.debug shouldBe false
    }

    @Test
    fun `do not require 'buildSrc' directory by default`() {
        setup.buildSrcCopy shouldBe null
    }

    @Test
    fun `enable debug mode`() {
        setup.enableRunnerDebug()
        setup.debug shouldBe true
    }

    @Test
    fun `add plugin under test classpath`() {
        setup.withPluginClasspath()
        setup.withDefaultPluginClasspath shouldBe true
    }

    @Test
    fun `add custom plugin classpath elements`() {
        setup.withPluginClasspath(File("custom.jar"))

        setup.additionalClasspathElements shouldContain File("custom.jar")
        setup.withDefaultPluginClasspath shouldBe false
    }

    @Test
    fun `reject empty custom classpath elements`() {
        assertThrows<IllegalArgumentException> {
            setup.withPluginClasspath(*emptyArray())
        }
    }

    @Test
    fun `use a shared TestKit directory when instructed`() {
        setup.useSharedTestKit shouldBe false
        setup.withSharedTestKitDirectory()
        setup.useSharedTestKit shouldBe true
    }

    @Test
    fun `copy 'buildSrc' without caching`() {
        setup.copyBuildSrc(cachingEnabled = false)
        setup.buildSrcCopy shouldNotBe null
    }

    @Nested
    inner class `accept a resource directory` {

        @Test
        fun `with a Java 'Predicate'`() {
            val predicate = Predicate<Path> { it.toString().contains(".java") }
            setup.fromResources("some_dir", predicate)

            setup.resourceDir shouldBe "some_dir"
            setup.matching(Paths.get("Foo.java")) shouldBe true
            setup.matching(Paths.get("Foo.txt")) shouldBe false
        }

        @Test
        fun `with file names`() {
            setup.fromResources("some_dir", "Foo.java", "Bar.java")

            setup.resourceDir shouldBe "some_dir"
            setup.matching(Paths.get("a/Foo.java")) shouldBe true
            setup.matching(Paths.get("a/Other.java")) shouldBe false
        }
    }

    @Nested
    inner class `not allow setting both 'debug' mode and environment variables` {

        @Test
        fun `when 'debug' already set`() {
            setup.enableRunnerDebug()
            assertThrows<IllegalStateException> {
                setup.withEnvironment(mapOf("foo" to "bar"))
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

        val args = commandLineArgs
        args shouldContain name
        args shouldContain value
    }

    @Nested
    inner class `turn logging level` {

        private val debugOption = "--debug"

        @Test
        fun `having it off by default`() {
            commandLineArgs shouldNotContain debugOption
        }

        @Test
        fun `when instructed`() {
            setup.withLoggingLevel(LogLevel.DEBUG)
            commandLineArgs shouldContain debugOption
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

            val args = commandLineArgs
            args shouldContain options[0]
            args shouldContain options[1]
        }

        @Test
        fun `pass as 'Iterable'`() {
            setup.withOptions(options)
            val args = commandLineArgs
            options.forEach {
                args shouldContain it
            }
        }
    }
}
