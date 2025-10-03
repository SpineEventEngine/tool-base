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

import io.spine.annotation.VisibleForTesting
import org.gradle.api.logging.LogLevel

/**
 * A command line option passed to a Gradle runner.
 */
internal data class CliOption(val name: String) {

    init {
        require(name.isNotBlank())
    }

    companion object {

        internal const val prefix = "--"

        @JvmField
        val stacktrace: CliOption = CliOption("stacktrace")

        @JvmField
        @Deprecated("Please use `GradleProjectSetup.withLogging(LogLevel)`.")
        @Suppress("unused")
        val debug: CliOption = CliOption("debug")

        @JvmField
        val noDaemon: CliOption = CliOption("no-daemon")
    }

    /**
     * Obtains the name of this option prefixed with `--` to be used as command line argument.
     */
    fun argument(): String = prefix + name

    /**
     * Obtains the value for passing in a command line.
     *
     * @see [argument]
     */
    override fun toString(): String = argument()
}

/**
 * Turns this [LogLevel] into a command line option passed to the Gradle process.
 */
@VisibleForTesting
internal fun LogLevel.toCliOption() = CliOption(name.lowercase())
