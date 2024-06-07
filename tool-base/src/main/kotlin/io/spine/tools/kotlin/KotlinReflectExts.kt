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

package io.spine.tools.kotlin

import kotlin.reflect.KClass

/**
 * The packages imported by default to every Kotlin file.
 *
 * @see <a href="https://kotlinlang.org/docs/packages.html#default-imports">Default imports</a>
 */
public val defaultPackages: List<String> by lazy {
    listOf(
        "kotlin",
        "kotlin.annotation",
        "kotlin.collections",
        "kotlin.comparisons",
        "kotlin.io",
        "kotlin.ranges",
        "kotlin.sequences",
        "kotlin.text",
        "kotlin.jvm",
        "java.lang"
    )
}

/**
 * Obtains the code which is used for referencing this Kotlin class in the _Kotlin_ code.
 *
 * For referencing a Kotlin class from the Java code, please
 * use [KClass.java.reference][io.spine.tools.java.reference].
 *
 * @return [KClass.simpleName] for the class belonging to [defaultPackages].
 *         Otherwise, [KClass.qualifiedName] is returned.
 * @see io.spine.tools.java.reference
 */
public val <T: Any> KClass<T>.reference: String
    get() {
        return if (packageName in defaultPackages) {
            simpleName ?: "$this"
        } else {
            qualifiedName ?: "$this"
        }
    }

/*
 * This is the shortcut for obtaining a package for the Kotlin class.
 *
 * At the time of writing, there's now a reliable way known to the authors
 * to obtain a package in cross-platform way in Kotlin without resorting to
 * `expect/actual` mechanism for platform detection.
 *
 * We assume that classes using this extension would be mostly from the platform
 * or, even if they are nested inside other classes, their references would
 * be fully qualified anyway.
 *
 * The value returns a substring before the last '.' in the [qualifiedName] of the class or
 * an empty string if the [qualifiedName] is not available.
 */
private val <T: Any> KClass<T>.packageName: String
    get() = qualifiedName?.substringBeforeLast('.') ?: ""
