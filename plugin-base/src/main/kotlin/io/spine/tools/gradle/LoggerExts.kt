/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.tools.gradle

import org.gradle.api.logging.Logger

/**
 * Prints a lazily evaluated message, if `debug` logging is enabled.
 */
public inline fun Logger.debug(message: () -> String) {
    if (isDebugEnabled) {
        debug(message())
    }
}

/**
 * Prints a lazily evaluated message, if `info` logging is enabled.
 */
public inline fun Logger.info(message: () -> String) {
    if (isInfoEnabled) {
        info(message())
    }
}

/**
 * Prints a lazily evaluated message, if `warn` logging is enabled.
 */
public inline fun Logger.warn(message: () -> String) {
    if (isWarnEnabled) {
        warn(message())
    }
}

/**
 * Prints a lazily evaluated message, if `error` logging is enabled.
 */
public inline fun Logger.error(message: () -> String) {
    if (isErrorEnabled) {
        error(message())
    }
}

/**
 * Executes the function [fn] and logs the amount of time in milliseconds it took.
 *
 * The logging level for the message is `debug`. If `debug` logging is not enabled,
 * the function is still executed.
 *
 * @param action The name of the action to invoke.
 * @param fn The action.
 * @return the result of invoking the action.
 */
@Suppress("ImplicitDefaultLocale", "MagicNumber") // intended in `String.format()` below.
public inline fun <T> Logger.logTime(action: String, fn: () -> T): T {
    val startNs = System.nanoTime()
    val result = fn()
    val endNs = System.nanoTime()

    val timeNs = endNs - startNs
    val timeMs = timeNs.toDouble() / 1_000_000

    debug { String.format("`%s` took %.2f ms.", action, timeMs) }

    return result
}
