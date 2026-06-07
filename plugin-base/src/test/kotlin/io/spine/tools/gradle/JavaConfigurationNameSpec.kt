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
import io.spine.tools.code.SourceSetName.Companion.test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`JavaConfigurationName` should")
internal class JavaConfigurationNameSpec {

    @Test
    fun `provide names of the 'main' source set configurations`() {
        JavaConfigurationName.let {
            it.annotationProcessor.name() shouldBe "annotationProcessor"
            it.api.name() shouldBe "api"
            it.implementation.name() shouldBe "implementation"
            it.compileOnly.name() shouldBe "compileOnly"
            it.compileClasspath.name() shouldBe "compileClasspath"
            it.runtimeOnly.name() shouldBe "runtimeOnly"
            it.runtimeClasspath.name() shouldBe "runtimeClasspath"
        }
    }

    @Test
    fun `provide names of the 'test' source set configurations`() {
        JavaConfigurationName.let {
            it.testAnnotationProcessor.name() shouldBe "testAnnotationProcessor"
            it.testImplementation.name() shouldBe "testImplementation"
            it.testCompileClasspath.name() shouldBe "testCompileClasspath"
            it.testRuntimeOnly.name() shouldBe "testRuntimeOnly"
            it.testRuntimeClasspath.name() shouldBe "testRuntimeClasspath"
        }
    }

    @Test
    @Suppress("DEPRECATION") // Cover deprecated, but still declared, configurations.
    fun `provide names of standalone configurations`() {
        JavaConfigurationName.let {
            it.classpath.name() shouldBe "classpath"
            it.protobuf.name() shouldBe "protobuf"
            it.fetch.name() shouldBe "fetch"
            it.compile.name() shouldBe "compile"
        }
    }

    @Test
    fun `compose configuration names for an arbitrary source set`() {
        JavaConfigurationName.let {
            it.annotationProcessor(test).name() shouldBe "testAnnotationProcessor"
            it.implementation(test).name() shouldBe "testImplementation"
            it.compileClasspath(test).name() shouldBe "testCompileClasspath"
            it.runtimeOnly(test).name() shouldBe "testRuntimeOnly"
            it.runtimeClasspath(test).name() shouldBe "testRuntimeClasspath"
        }
    }

    @Test
    fun `expose 'value' as an alias of 'name'`() {
        JavaConfigurationName.api.value() shouldBe "api"
    }
}
