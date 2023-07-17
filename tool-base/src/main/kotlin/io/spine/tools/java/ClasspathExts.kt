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

@file:JvmName("Classpaths")

package io.spine.tools.java

import io.spine.tools.java.code.Classpath
import io.spine.tools.java.code.classpath
import java.io.File.pathSeparator
import java.lang.System.lineSeparator

/**
 * Creates a new instance of [Classpath] parsing its items
 * from the given string.
 */
public fun parseClasspath(cp: String): Classpath {
    val items = cp.split(pathSeparator)
    return classpath {
        item.addAll(items)
    }
}

/**
 * Prints classpath to a text block putting each item on a separate line
 * finished with [pathSeparator].
 */
public fun Classpath.printItems(): String {
    return itemList.joinToString(pathSeparator + lineSeparator())
}

/**
 * Obtains all items of this classpath.
 *
 * This method is a shortcut for [Classpath.getItemList].
 *
 * @see [jars]
 */
public fun Classpath.items(): List<String> = itemList

/**
 * Obtains [items] of the classpath which are JAR files.
 *
 * @see [items]
 */
public fun Classpath.jars(): List<String> {
    return itemList.filter { it.endsWith(".jar") }
}
