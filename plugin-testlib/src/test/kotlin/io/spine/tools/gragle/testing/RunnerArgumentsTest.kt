/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import com.google.common.truth.Truth.assertThat
import io.spine.tools.gradle.task.JavaTaskName.Companion.compileJava
import io.spine.tools.gradle.testing.CliOption.Companion.debug
import io.spine.tools.gradle.testing.CliOption.Companion.noDaemon
import io.spine.tools.gradle.testing.CliOption.Companion.stacktrace
import io.spine.tools.gradle.testing.CliProperty
import io.spine.tools.gradle.testing.RunnerArguments
import org.junit.jupiter.api.Test

class `'RunnerArguments' should` {

    @Test
    fun `contain task name`() {
        val args = RunnerArguments().forTask(compileJava)
        assertThat(args).containsExactly(
            compileJava.name(),
            stacktrace.argument()
        )
    }

    @Test
    fun `contain debug flag`() {
        val args = RunnerArguments().withDebug().forTask(compileJava)
        assertThat(args).containsExactly(
            compileJava.name(),
            stacktrace.argument(),
            debug.argument()
        )
    }

    @Test
    fun `contain Gradle properties`() {
        val args = RunnerArguments().withProperties(
            mapOf(
                "foo1" to "bar1",
                "foo2" to "bar2"
            )
        ).forTask(compileJava)

        assertThat(args).containsExactly(
            compileJava.name(),
            stacktrace.argument(),
            CliProperty("foo1", "bar1").argument(),
            CliProperty("foo2", "bar2").argument()
        )
    }

    @Test
    fun `turn off stacktrace output`() {
        val args = RunnerArguments().noStacktrace().forTask(compileJava)
        assertThat(args).doesNotContain(stacktrace.argument())
    }

    @Test
    fun `turn off daemons mode`() {
        val args = RunnerArguments().noDaemon().forTask(compileJava)
        assertThat(args).contains(noDaemon.argument())
    }
}
