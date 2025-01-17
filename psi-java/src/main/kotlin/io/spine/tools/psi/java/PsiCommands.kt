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
import kotlin.system.exitProcess

/**
 * Executes the given [runnable] as a PSI modification [command][CommandProcessor.executeCommand],
 * ensuring any errors thrown by the [runnable] are handled using the provided [errorHandler].
 *
 * By default, the `CoreCommandProcessor` used for PSI command processing suppresses
 * any [Throwable] thrown by the command, making it difficult for PSI users to detect
 * and handle errors.
 *
 * To address this, the given [runnable] is wrapped in a try-catch block.
 * Any thrown errors or exceptions are passed to the provided [errorHandler].
 *
 * If no custom [errorHandler] is provided, the default behavior is to print the stack
 * trace to [System.err] and terminate the process with the exit code `1`.
 *
 * @param runnable The [Runnable] to execute as a PSI modification.
 * @param errorHandler A lambda to handle any [Throwable] thrown by the [runnable].
 *  Defaults to printing the stack trace and terminating the process with the exit code `1`.
 */
@JvmOverloads
@JvmName("execute")
@Suppress("TooGenericExceptionCaught") // We need everything, including `java.lang.Error`.
public fun execute(errorHandler: (Throwable) -> Unit = ::printAndTerminate, runnable: Runnable) {
    val withHandledErrors = Runnable {
        try {
            runnable.run()
        } catch (t: Throwable) {
            errorHandler(t)
        }
    }
    commandProcessor.executeCommand(project, withHandledErrors, null, null)
}

private fun printAndTerminate(t: Throwable) {
    t.printStackTrace()
    exitProcess(1)
}
