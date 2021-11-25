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
public class RunnerArguments private constructor(
    /** The name of the task to be executed by the runner. */
    private val task: TaskName,

    /** If `true`, [CliOption.debug] will be passed to the runner.  */
    private val debug: Boolean = false,

    /** If `true`, [CliOption.stacktrace] will be passed to the runner. */
    private val stacktrace: Boolean = true,

    /** If `true`, [CliOption.noDaemon] will be passed to the runner. */
    private val noDaemon: Boolean = false
) {

    public companion object {

        /** Creates new instance or runner arguments for the specified task. */
        @JvmStatic
        public fun forTask(task: TaskName): RunnerArguments = RunnerArguments(task)
    }

    /** Turns on the debug flag. */
    public fun withDebug(): RunnerArguments {
        return RunnerArguments(task,true, stacktrace)
    }

    /** Turns off the stacktrace output. */
    public fun noStacktrace(): RunnerArguments {
        return RunnerArguments(task, debug, false)
    }

    /** Turns on the `--no-daemon` flag. */
    public fun noDaemon(): RunnerArguments {
        return RunnerArguments(task, debug, stacktrace, true)
    }

    /** Obtains arguments as an array. */
    public fun toArray(): Array<String> = taskWithOptions().toTypedArray()

    /** Applies passed properties and returns resulting array of command line arguments. */
    public fun apply(properties: Map<String, String>): Array<String> {
        val args: MutableList<String> = taskWithOptions()
        properties.forEach { (name, value) ->
            args.add("-P${name}=${value}")
        }
        return args.toTypedArray()
    }

    private fun taskWithOptions(): MutableList<String> {
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
