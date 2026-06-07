/*
 * Copyright 2026, TeamDev. All rights reserved.
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

package io.spine.tools.gradle

import io.kotest.matchers.shouldBe
import io.spine.code.proto.FileSet
import io.spine.tools.code.Indent
import java.util.function.Supplier
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`CodeGenerationAction` should")
internal class CodeGenerationActionSpec {

    private lateinit var project: Project
    private lateinit var action: StubAction

    @BeforeEach
    fun setUp() {
        project = ProjectBuilder.builder().build()
        action = StubAction(project)
    }

    @Test
    fun `expose the project`() {
        action.exposedProject() shouldBe project
    }

    @Test
    fun `expose the supplier of proto files`() {
        action.exposedProtoFiles() shouldBe StubAction.protoFilesSupplier
    }

    @Test
    fun `lazily compute the indentation via the abstract method`() {
        action.exposedIndent() shouldBe Indent.of4()
    }

    @Test
    fun `compute absolute directories for proto sources and the target`() {
        action.let {
            it.exposedProtoSrcDir().isAbsolute shouldBe true
            it.exposedTargetDir().isAbsolute shouldBe true
        }
    }
}

/**
 * A minimal concrete subclass exposing the `protected` API of
 * [CodeGenerationAction] for testing.
 */
private class StubAction(project: Project) : CodeGenerationAction(
    project,
    protoFilesSupplier,
    Supplier { "build/generated" },
    Supplier { "src/main/proto" }
) {
    override fun getIndent(project: Project): Indent = Indent.of4()

    override fun execute(t: Task) = Unit

    fun exposedProject() = project()
    fun exposedProtoFiles() = protoFiles()
    fun exposedIndent() = indent()
    fun exposedProtoSrcDir() = protoSrcDir()
    fun exposedTargetDir() = targetDir()

    companion object {
        val protoFilesSupplier: Supplier<FileSet> = Supplier { FileSet.of(emptyList()) }
    }
}
