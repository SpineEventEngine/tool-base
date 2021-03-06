/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.task

import com.google.common.truth.Truth.assertThat
import io.spine.tools.code.SourceSetName
import io.spine.tools.code.SourceSetName.Companion.main
import io.spine.tools.code.SourceSetName.Companion.test
import io.spine.tools.gradle.task.JavaTaskName.Companion.classes
import io.spine.tools.gradle.task.JavaTaskName.Companion.compileJava
import io.spine.tools.gradle.task.JavaTaskName.Companion.compileTestJava
import io.spine.tools.gradle.task.JavaTaskName.Companion.processResources
import io.spine.tools.gradle.task.JavaTaskName.Companion.processTestResources
import io.spine.tools.gradle.task.JavaTaskName.Companion.testClasses
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class `'JavaTaskName' should` {

    @Nested
    inner class `provide 'compileJava' task for` {

        @Test
        fun `'main' source set`() {
            assertThat(compileJava(main).name())
                .isEqualTo("compileJava")
        }

        @Test
        fun `'test' source set`() {
            assertThat(compileJava(test).name())
                .isEqualTo("compileTestJava")
        }

        @Test
        fun `custom source set`() {
            assertThat(compileJava(SourceSetName("somethingSpecial")).name())
                .isEqualTo("compileSomethingSpecialJava")
        }
    }

    @Nested
    inner class `provide 'classes' task for` {

        @Test
        fun `'main' source set`() {
            assertThat(classes(main).name())
                .isEqualTo("classes")
        }

        @Test
        fun `'test' source set`() {
            assertThat(classes(test).name())
                .isEqualTo("testClasses")
        }

        @Test
        fun `custom source set`() {
            assertThat(classes(SourceSetName("blueWaterline")).name())
                .isEqualTo("blueWaterlineClasses")
        }
    }

    @Nested
    inner class `provide 'processResources' task for` {

        @Test
        fun `'main' source set`() {
            assertThat(processResources(main).name())
                .isEqualTo("processResources")
        }

        @Test
        fun `'test' source set`() {
            assertThat(processResources(test).name())
                .isEqualTo("processTestResources")
        }

        @Test
        fun `custom source set`() {
            assertThat(processResources(SourceSetName("goldMine")).name())
                .isEqualTo("processGoldMineResources")
        }
    }

    @Test
    fun `expose properties for compatibility with the previous versions`() {
        assertThat(compileJava).isEqualTo(compileJava(main))
        assertThat(compileTestJava).isEqualTo(compileJava(test))
        assertThat(classes).isEqualTo(classes(main))
        assertThat(testClasses).isEqualTo(classes(test))
        assertThat(processResources).isEqualTo(processResources(main))
        assertThat(processTestResources).isEqualTo(processResources(test))
    }
}
