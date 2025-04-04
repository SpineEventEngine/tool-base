/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.testing

import java.io.File
import java.net.URL
import org.gradle.api.Plugin

/**
 * Obtains the directory or a JAR file containing the Gradle plugin
 * definition file for the given [pluginId].
 */
public fun Class<Plugin<*>>.resourceDir(pluginId: String): File {
    val pluginDescriptionUrl = classLoader.getResource(
        "META-INF/gradle-plugins/${pluginId}.properties"
    )!!
    // The below climbing up via `parentFile` ends in the directory containing `META-INF`.
    // This is what we need to pass to `GradleRunner.withPluginClasspath(...)` when
    // testing a plugin in a source set other than `main`.
    val pluginDir = File(pluginDescriptionUrl).parentFile.parentFile.parentFile
    return pluginDir
}

/**
 * Creates new [File] instance assuming that the given [url] refers to a file.
 */
private fun File(url: URL): File {
    val path = url.file.removeFileProtocolPrefix()
    return File(path)
}

/**
 * Removes the `file:/` and `file:\` prefixes in this string.
 *
 * The prefix is present in the results of [URL.getFile] method.
 * We need to remove the prefix when creating new [File] instance.
 */
private fun String.removeFileProtocolPrefix(): String {
    val protocol = "file:"
    return replace("$protocol/", "").replace("$protocol\\", "")
}
