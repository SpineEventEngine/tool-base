/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package io.spine.tools.protobuf.gradle.plugin

import io.spine.tools.gradle.testing.Gradle
import io.spine.tools.gradle.testing.under
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

/**
 * The abstract base for test suites of the plugins configuring Protobuf.
 */
abstract class ProtobufPluginTest {

    protected val File.protoDir: File get() = File(this, "src/main/proto")
    protected val File.generatedJava: File get() = File(this, "generated/main/java")

    protected abstract val group: String
    protected abstract val version: String

    protected lateinit var projectDir: File

    protected val protoDir: File get() = projectDir.protoDir
    protected val generatedJava: File get() = projectDir.generatedJava
    protected val buildGeneratedJava: File get() =
        projectDir.resolve("build/generated/sources/proto/main/java")

    @BeforeEach
    fun setupProjectDirectory(@TempDir projectDir: File) {
        this.projectDir = projectDir
        Gradle.settingsFile.under(projectDir).writeText("")
        protoDir.mkdirs()
    }

    /**
     * Creates a minimal proto file in the [protoDir].
     */
    protected fun writeSampleProto() {
        File(protoDir, "sample.proto").writeText(
            """
            syntax = "proto3";
            package sample;
            message Msg {}
            """.trimIndent()
        )
    }
}
