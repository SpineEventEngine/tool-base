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

package io.spine.tools.psi

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@DisplayName("`IdeaStandaloneExecution` should")
internal class IdeaStandaloneExecutionSpec {

    @Test
    fun `set up the system properties for the standalone PSI execution`() {
        // The headless flag is forced on only when it was undefined, so capture
        // its prior value to assert that exact contract below.
        val headlessBefore = System.getProperty("java.awt.headless")

        IdeaStandaloneExecution.setUp()

        System.getProperty("idea.io.use.nio2") shouldBe "true"
        System.getProperty("project.structure.add.tools.jar.to.new.jdk") shouldBe "false"
        System.getProperty("psi.track.invalidation") shouldBe "true"
        System.getProperty("psi.incremental.reparse.depth.limit") shouldBe "1000"
        System.getProperty("ide.hide.excluded.files") shouldBe "false"
        System.getProperty("ast.loading.filter") shouldBe "false"
        System.getProperty("idea.ignore.disabled.plugins") shouldBe "true"
        System.getProperty("idea.plugins.compatible.build") shouldBe "999.SNAPSHOT"

        // `java.awt.headless` is set to `"true"` only if it was previously
        // undefined; an environment-provided value is left untouched.
        val headlessAfter = System.getProperty("java.awt.headless")
        if (headlessBefore == null) {
            headlessAfter shouldBe "true"
        } else {
            headlessAfter shouldBe headlessBefore
        }
    }

    @Test
    fun `be idempotent across repeated calls`() {
        IdeaStandaloneExecution.setUp()
        assertDoesNotThrow {
            // The second call must short-circuit on the `configured` flag.
            IdeaStandaloneExecution.setUp()
        }
    }
}
