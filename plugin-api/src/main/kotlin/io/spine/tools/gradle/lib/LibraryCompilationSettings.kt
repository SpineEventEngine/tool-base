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

package io.spine.tools.gradle.lib

import com.google.protobuf.Message

/**
 * The base interface of compilation settings introduced by [LibraryPlugin]s.
 *
 * Implementing classes will be extensions for settings of a [LibraryPlugin].
 * As such, they will use Gradle API for properties and action blocks.
 * To be passed to a Spine Compiler Plugin of a library the settings need to be
 * converted to a Protobuf [Message].
 *
 * Implementing classes need to supply the conversion by implementing the [toProto] function.
 *
 * Compilation settings come under the same name [compile][EXTENSION_NAME].
 *
 * @param T The type of the Protobuf message which passes the settings to a Spine Compiler plugin.
 */
public interface LibraryCompilationSettings<T : Message> : ConvertableExtension<T> {

    public companion object {

        /**
         * The name of the extension created under the settings of a library for
         * tuning the compilation process of a Spine Compiler plugin of the library.
         */
        public const val EXTENSION_NAME: String = "compile"
    }
}
