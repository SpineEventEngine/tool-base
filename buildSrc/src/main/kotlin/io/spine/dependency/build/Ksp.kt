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

package io.spine.dependency.build

/**
 * Kotlin Symbol Processing API.
 *
 * @see <a href="https://github.com/google/ksp">KSP GitHub repository</a>
 */
@Suppress("ConstPropertyName")
object Ksp {

    /**
     * The latest version compatible with Kotlin v1.8.22, which is bundled with Gradle 7.6.4.
     *
     * We need to stick to this version until we migrate to newer Gradle.
     * Trying to use a newer version results in the following console output:
     * ```
     * ksp-1.9.24-1.0.20 is too new for kotlin-1.8.22. Please upgrade kotlin-gradle-plugin to 1.9.24.
     * ```
     *
     * The version compatible with Kotlin v1.9.24 compiler is 1.9.24-1.0.20.
     */
    const val version = "1.8.22-1.0.11"
    const val id = "com.google.devtools.ksp"
    const val group = "com.google.devtools.ksp"
    const val symbolProcessingApi = "$group:symbol-processing-api:$version"
    const val symbolProcessing = "$group:symbol-processing:$version"
}
