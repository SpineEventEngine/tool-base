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

package io.spine.tools.gradle.protobuf

import io.spine.tools.gradle.protobuf.ProtobufDependencies.sourceSetExtensionName
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet

/**
 * Tells if this [SourceSet] contains `.proto` files.
 *
 * @return true if there is [protoDirectorySet] available in this source set, _and_ the set has
 *         at least one file. Otherwise, false.
 */
public fun SourceSet.containsProtoFiles(): Boolean {
    val protoDirectorySet = protoDirectorySet()
        ?: return false // no `proto` extension at all.
    val isEmpty = protoDirectorySet.files.isEmpty()
    return !isEmpty
}

/**
 * Obtains a [SourceDirectorySet] containing `.proto` files in this [SourceSet].
 *
 * @return the directory set or `null`, if there is no `proto` extension added to this `SourceSet`
 *         by the Protobuf Gradle Plugin.
 * @see ProtobufDependencies.sourceSetExtensionName
 */
public fun SourceSet.protoDirectorySet(): SourceDirectorySet? {
    return extensions.getByName(sourceSetExtensionName)
        .let { ext -> ext as? SourceDirectorySet }
}
