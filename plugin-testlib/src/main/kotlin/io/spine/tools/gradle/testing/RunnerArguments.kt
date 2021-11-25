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
package io.spine.tools.gradle.testing

import io.spine.tools.gradle.task.TaskName

/**
 * Create Gradle Runner arguments for a task.
 */
internal class RunnerArguments internal constructor(

    /** If `true`, [CliOption.debug] will be passed to the runner.  */
    private val debug: Boolean = false,

    /** If `true`, [CliOption.stacktrace] will be passed to the runner. */
    private val stacktrace: Boolean = true,

    /** If `true`, [CliOption.noDaemon] will be passed to the runner. */
    private val noDaemon: Boolean = false,

    /** Properties passed to the runner. */
    private val properties: Map<String, String> = mapOf()
) {

    /** Turns on the debug flag. */
    fun withDebug(): RunnerArguments {
        return RunnerArguments(
            debug = true,
            stacktrace = this.stacktrace,
            noDaemon = this.noDaemon,
            properties = this.properties
        )
    }

    /** Turns off the stacktrace output. */
    fun noStacktrace(): RunnerArguments {
        return RunnerArguments(
            debug = this.debug,
            stacktrace = false,
            noDaemon = this.noDaemon,
            properties = this.properties
        )
    }

    /** Turns on the `--no-daemon` flag. */
    fun noDaemon(): RunnerArguments {
        return RunnerArguments(
            debug = this.debug,
            stacktrace = this.stacktrace,
            noDaemon = true,
            properties = this.properties
        )
    }

    /** Adds a Gradle property entry to the command line arguments. */
    fun withProperty(name: String, value: String): RunnerArguments {
        require(name.isNotEmpty())
        require(name.isNotBlank())
        require(value.isNotBlank())
        return RunnerArguments(
            debug = this.debug,
            stacktrace = this.stacktrace,
            noDaemon = this.noDaemon,
            properties = this.properties + Pair(name, value)
        )
    }

    /** Adds passed properties to the arguments. */
    fun withProperties(properties: Map<String, String>): RunnerArguments{
        return RunnerArguments(
            debug = this.debug,
            stacktrace = this.stacktrace,
            noDaemon = this.noDaemon,
            properties = this.properties + properties
        )
    }

    /**
     * Adds the passed properties to those that may be already applied and returns
     * resulting array of command line arguments.
     */
    fun forTask(task: TaskName): Array<String> {
        val args: MutableList<String> = taskWithOptions(task)
        properties.forEach { entry ->
            args.add(CliProperty(entry).argument())
        }
        return args.toTypedArray()
    }

    private fun taskWithOptions(task: TaskName): MutableList<String> {
        val args: MutableList<String> = mutableListOf(task.name())
        if (debug) {
            args.add(CliOption.debug.argument())
        }
        if (stacktrace) {
            args.add(CliOption.stacktrace.argument())
        }
        if (noDaemon) {
            args.add(CliOption.noDaemon.argument())
        }
        return args
    }
}
