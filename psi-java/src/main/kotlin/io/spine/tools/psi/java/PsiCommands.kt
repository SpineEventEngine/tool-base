/*
 * Copyright 2024, TeamDev. All rights reserved.
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

package io.spine.tools.psi.java

import com.intellij.openapi.command.CommandProcessor
import io.spine.tools.psi.java.Environment.commandProcessor
import io.spine.tools.psi.java.Environment.project
import java.lang.Thread.getDefaultUncaughtExceptionHandler

/**
 * Executes the given [Runnable] as a PSI modification
 * [command][CommandProcessor.executeCommand].
 */
@JvmName("execute")
public fun execute(runnable: Runnable) {
    val command = withRedirectedErrors(runnable)
    commandProcessor.executeCommand(project, command, null, null)
}

/**
 * Wraps the given [runnable] in a try-catch block, redirecting any [Throwable]
 * to the default uncaught exception handler.
 *
 * The `CoreCommandProcessor` used by PSI for command handling swallows any
 * [Throwable] from the passed [Runnable]. As a result, PSI users cannot know
 * if an error has occurred. For example, ProtoData is expected to print
 * the stacktrace and exist the application in case of error or exception,
 * but this behavior is suppressed by PSI.
 *
 * This method addresses this issue by wrapping the given [runnable] in its own
 * try-catch block, redirecting all errors and exceptions to the default handler.
 * This ensures that PSI does not swallow these exceptions. However, as a consequence,
 * the default exception handler must be explicitly set, as redirection only works
 * when an explicit handler is in place.
 *
 * Note: to make this method work, the default exception handler must be set explicitly
 * using [Thread.setDefaultUncaughtExceptionHandler]. Otherwise, a [NullPointerException]
 * will be thrown and swallowed by PSI, making this method ineffective.
 */
@Suppress("TooGenericExceptionCaught")
private fun withRedirectedErrors(runnable: Runnable) = Runnable {
    try {
        runnable.run()
    } catch (e: Throwable) {
        val currentThread = Thread.currentThread()
        val globalHandler = getDefaultUncaughtExceptionHandler()
        globalHandler.uncaughtException(currentThread, e)
    }
}
