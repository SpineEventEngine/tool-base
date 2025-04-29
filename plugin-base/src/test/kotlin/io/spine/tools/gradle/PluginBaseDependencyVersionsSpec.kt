/*
 * Copyright 2025, TeamDev. All rights reserved.
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

import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.spine.tools.gradle.protobuf.ProtobufDependencies.protobufCompiler
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("plugin-base `DependencyVersions` should")
internal class PluginBaseDependencyVersionsSpec {

    private val versions: DependencyVersions =
        DependencyVersions.ofPluginBase

    @Test
    @DisplayName("contain the version of Protobuf compiler")
    fun containProtoc() {
        val version = versions.versionOf(protobufCompiler)

        version shouldBePresent {
            it shouldMatch "^[3|4]\\.\\d+\\.\\d+"
        }
    }

    @Test
    @DisplayName("contain the version the module itself")
    fun containOwnVersion() {
        val protoc = ThirdPartyDependency(Artifact.SPINE_TOOLS_GROUP, Artifact.PLUGIN_BASE_ID)
        val version = versions.versionOf(protoc)

        version shouldBePresent {
            it shouldNotBe ""
        }
    }
}
