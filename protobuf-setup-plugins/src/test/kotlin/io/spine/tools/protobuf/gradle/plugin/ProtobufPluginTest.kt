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
}
