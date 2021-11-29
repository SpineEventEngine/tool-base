/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.gragle.testing

import com.google.common.testing.NullPointerTester
import com.google.common.truth.Truth.assertThat
import io.spine.tools.gradle.testing.GradleProject
import io.spine.tools.gradle.testing.GradleProjectSetup
import io.spine.tools.gradle.testing.KGradleProjectSetup
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class `'GradleProjectSetup' should` {

    private lateinit var projectDir: File
    private lateinit var instance: GradleProjectSetup
    private lateinit var setup: KGradleProjectSetup

    @BeforeEach
    fun setup(@TempDir projectDir: File) {
        this.projectDir = projectDir
        instance = GradleProject.setupAt(projectDir)
        setup = KGradleProjectSetup(instance)
    }

    @Test
    fun `not accept 'null' arguments`() {
        NullPointerTester().testAllPublicInstanceMethods(instance)
    }

    @Test
    fun `provide project directory`() {
        assertThat(setup.projectDir())
            .isEqualTo(projectDir)
    }

    @Test
    fun `have 'debug' turned off by default`() {
        assertThat(setup.debug())
            .isFalse()
    }

    @Test
    fun `do not require 'buildSrc' directory by default`() {
        assertThat(setup.needsBuildSrc())
            .isFalse()
    }

    @Test
    fun `enable debug mode`() {
        instance.enableDebug()
        assertThat(setup.debug())
            .isTrue()
    }

    @Test
    fun `add plugin under test classpath`() {
        instance.withPluginClasspath()
        assertThat(setup.addPluginUnderTestClasspath())
            .isTrue()
    }
}
