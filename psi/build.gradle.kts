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

import io.spine.dependency.local.Base
import io.spine.dependency.local.TestLib
import io.spine.gradle.report.coverage.creditTestCoverageFrom

plugins {
    module
}

dependencies {
    api(Base.lib)
    api(project(":intellij-platform"))
    testImplementation(TestLib.lib)
}

/**
 * The language-neutral PSI classes of this module are exercised by the
 * Java-PSI test fixtures in `psi-java`. Credit that coverage to this module's
 * own Kover report, which otherwise sees only this module's `test` data.
 */
creditTestCoverageFrom(project(":psi-java"))

/**
 * The `intellij-platform` module assembles its artifact with the Shadow plugin
 * (via `uber-jar-module`), which disables the regular `jar` task. A consumer that
 * puts that JAR on its runtime classpath — such as this module's `test` task —
 * therefore does not get an automatic task dependency on `:intellij-platform:shadowJar`,
 * which Gradle's task-output validation rejects. Declare it explicitly, mirroring
 * the workaround already used in `uber-jar-module` for publishing.
 */
tasks.named("test") {
    dependsOn(":intellij-platform:shadowJar")
}
