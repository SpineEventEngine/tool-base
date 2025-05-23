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

package io.spine.tools.gradle.task

import io.spine.tools.code.SourceSetName
import io.spine.tools.code.SourceSetName.Companion.main
import io.spine.tools.code.SourceSetName.Companion.test
import io.spine.tools.gradle.protobuf.ProtobufTaskName

private const val NEW_CLASS_NAME = "io.spine.tools.gradle.protobuf.ProtobufTaskName"

private const val IMPORT = "NEW_CLASS_NAME"

/**
 * Names of Gradle tasks defined by the Protobuf Gradle plugin.
 *
 * @see <a href="https://github.com/google/protobuf-gradle-plugin">Protobuf Gradle plugin</a>
 */
@Deprecated(
    message = "Please use `$NEW_CLASS_NAME`.",
    replaceWith = ReplaceWith(
        "ProtobufTaskName",
        imports = [IMPORT]
    )
)
public class ProtobufTaskName(value: String, ssn: SourceSetName) :
    ProtobufTaskName(value, ssn) {

    public companion object {

        /**
         * Obtains a name of the `generateProto` task for the specified source set.
         */
        @Deprecated(
            message =
                "Please use `$NEW_CLASS_NAME.generateProto` instead.",
            replaceWith = ReplaceWith(
                "ProtobufTaskName.generateProto",
                imports = [IMPORT]
            )
        )
        @JvmStatic
        public fun generateProto(ssn: SourceSetName): TaskName =
            ProtobufTaskName.generateProto(ssn)

        /**
         * Generates production code from Protobuf.
         *
         * Note that this task is not a public API of the plugin.
         * Users should be conscious and cautious when depending on it.
         */
        @Deprecated(
            message =
                "Please use `$NEW_CLASS_NAME.generateProto` instead.",
            replaceWith = ReplaceWith(
                "ProtobufTaskName.generateProto",
                imports = [IMPORT]
            )
        )
        @JvmField
        public val generateProto: TaskName = ProtobufTaskName.generateProto(main)

        /**
         * Generates test code from Protobuf.
         *
         * Note that this task is not a public API of the plugin.
         * Users should be conscious and cautious when depending on it.
         */
        @Deprecated(
            message =
                "Please use `$NEW_CLASS_NAME.generateTestProto` instead.",
            replaceWith = ReplaceWith(
                "ProtobufTaskName.generateTestProto",
                imports = [IMPORT]
            )
        )
        @JvmField
        public val generateTestProto: TaskName = ProtobufTaskName.generateProto(test)
    }
}
