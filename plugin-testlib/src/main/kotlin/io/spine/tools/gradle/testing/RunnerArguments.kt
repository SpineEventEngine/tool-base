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
package io.spine.tools.gradle.testing

import com.google.common.annotations.VisibleForTesting
import io.spine.tools.gradle.task.TaskName
import org.gradle.api.logging.LogLevel

/**
 * Create Gradle Runner arguments for a task.
 */
internal class RunnerArguments internal constructor(

    /**
     * The level of logging to be used in the runner.
     */
    private val loggingLevel: LogLevel = LogLevel.LIFECYCLE,

    /**
     * If `true`, [CliOption.stacktrace] will be passed to the runner.
     */
    private val stacktrace: Boolean = true,

    /**
     * If `true`, [CliOption.noDaemon] will be passed to the runner.
     */
    private val noDaemon: Boolean = false,

    /**
     * Properties passed to the runner.
     */
    private val properties: Map<String, String> = mapOf(),

    /**
     * Options passed to the runner.
     */
    private val options: List<String> = listOf()
) {

    /**
     * Turns on the debug flag.
     */
    fun withDebugLogging(): RunnerArguments {
        return RunnerArguments(
            loggingLevel = LogLevel.DEBUG,
            stacktrace = this.stacktrace,
            noDaemon = this.noDaemon,
            properties = this.properties,
            options = this.options
        )
    }

    /**
     * Turns off the stacktrace output.
     */
    fun noStacktrace(): RunnerArguments {
        return RunnerArguments(
            loggingLevel = this.loggingLevel,
            stacktrace = false,
            noDaemon = this.noDaemon,
            properties = this.properties,
            options = this.options
        )
    }

    /**
     * Turns on the `--no-daemon` flag.
     */
    fun noDaemon(): RunnerArguments {
        return RunnerArguments(
            loggingLevel = this.loggingLevel,
            stacktrace = this.stacktrace,
            noDaemon = true,
            properties = this.properties,
            options = this.options
        )
    }

    /**
     * Adds the logging level option.
     */
    fun withLoggingLevel(level: LogLevel): RunnerArguments {
        return RunnerArguments(
            loggingLevel = level,
            stacktrace = this.stacktrace,
            noDaemon = this.noDaemon,
            properties = this.properties,
            options = this.options
        )
    }

    /**
     * Adds a Gradle property entry to the command line arguments.
     */
    fun withProperty(name: String, value: String): RunnerArguments {
        require(name.isNotBlank())
        require(value.isNotBlank())
        return RunnerArguments(
            loggingLevel = this.loggingLevel,
            stacktrace = this.stacktrace,
            noDaemon = this.noDaemon,
            properties = this.properties + Pair(name, value),
            options = this.options
        )
    }

    /**
     * Adds passed properties to the arguments.
     */
    fun withProperties(properties: Map<String, String>): RunnerArguments {
        return RunnerArguments(
            loggingLevel = this.loggingLevel,
            stacktrace = this.stacktrace,
            noDaemon = this.noDaemon,
            properties = this.properties + properties,
            options = this.options
        )
    }

    /**
     * Adds passed options to the command line arguments.
     */
    fun withOptions(options: Iterable<String>): RunnerArguments {
        return RunnerArguments(
            loggingLevel = this.loggingLevel,
            stacktrace = this.stacktrace,
            noDaemon = this.noDaemon,
            properties = this.properties,
            options = this.options + options
        )
    }

    /**
     * Adds the passed properties to those that may be already applied and returns
     * resulting array of command line arguments.
     */
    fun forTask(task: TaskName): List<String> {
        val args: MutableList<String> = taskWithOptions(task)
        properties.forEach { entry ->
            args.add(CliProperty(entry).argument())
        }
        return args.toList()
    }

    private fun taskWithOptions(task: TaskName): MutableList<String> {
        val args: MutableList<String> = mutableListOf(task.name())
        if (loggingLevel != LogLevel.LIFECYCLE) {
            args.add(loggingLevel.toCommandLineOption())
        }
        if (stacktrace) {
            args.add(CliOption.stacktrace.argument())
        }
        if (noDaemon) {
            args.add(CliOption.noDaemon.argument())
        }
        args.addAll(options)
        return args
    }
}

/**
 * Turns this [LogLevel] into a command line option passed to the Gradle process.
 */
@VisibleForTesting
internal fun LogLevel.toCommandLineOption() = "--${name.lowercase()}"
