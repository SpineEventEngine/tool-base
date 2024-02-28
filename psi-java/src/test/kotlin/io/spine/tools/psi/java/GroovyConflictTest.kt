/*
 * Copyright 2024, TeamDev. All rights reserved.
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

package io.spine.tools.psi.java

import java.io.File
import java.net.URI
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName(
    "IntelliJ Platform dependencies should not affect Gradle `ProjectBuilder` API"
)
/**
 * This test makes sure that the `IntelliJ.Java.impl` dependency does not break
 * the `GradleProject` API we use in tests.
 *
 * The nature of the conflict is using different versions of Groovy in the classpath.
 */
internal class GroovyConflictTest {

    @Test
    fun `allowing to create a 'Project'`(@TempDir projectDir: File) {
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()
        // Add repositories for resolving locally built artifacts (via `mavenLocal()`)
        // and their dependencies via `mavenCentral()`.
        project.repositories.applyStandard()
        project.apply {
            it.plugin("java")
        }
    }
}

/**
 * Adds the standard Maven repositories to the receiver [RepositoryHandler].
 *
 * This is analogous to the eponymous method in the build scripts with the exception that this
 * method is available at the module's test runtime.
 *
 * Note that not all the Maven repositories may be added to the test projects, but only those that
 * are required for tests. We are not trying to keep these repositories is perfect synchrony with
 * the ones defined in build scripts.
 */
private fun RepositoryHandler.applyStandard() {
    mavenLocal()
    mavenCentral()
    val registryBaseUrl = "https://europe-maven.pkg.dev/spine-event-engine"
    maven {
        it.url = URI("$registryBaseUrl/releases")
    }
    maven {
        it.url = URI("$registryBaseUrl/snapshots")
    }
}
