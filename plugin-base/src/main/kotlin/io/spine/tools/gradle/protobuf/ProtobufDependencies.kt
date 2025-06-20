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

package io.spine.tools.gradle.protobuf

import io.spine.tools.gradle.PluginId
import io.spine.tools.gradle.ThirdPartyDependency
import io.spine.tools.proto.fs.Directory

/**
 * A factory of Protobuf-related artifact specs.
 */
public object ProtobufDependencies {

    private const val MAVEN_GROUP = "com.google.protobuf"

    /** The ID of the Protobuf Gradle plugin. */
    @JvmField
    public val gradlePlugin: PluginId = PluginId("com.google.protobuf")

    /** The name of the `SourceSet` extension installed by the Protobuf Gradle plugin. */
    @JvmField
    public val sourceSetExtensionName: String = Directory.rootName()

    /** The Protobuf Lite Java runtime library dependency. */
    @JvmField
    @Suppress("unused")
    public val protobufLite: ThirdPartyDependency =
        ThirdPartyDependency(MAVEN_GROUP, "protobuf-lite")

    /** The dependency on Protobuf Compiler. */
    @JvmField
    public val protobufCompiler: ThirdPartyDependency =
        ThirdPartyDependency(MAVEN_GROUP, "protoc")
}
