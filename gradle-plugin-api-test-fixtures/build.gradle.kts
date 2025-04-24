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

import io.spine.gradle.report.license.LicenseReporter

plugins {
    `maven-publish`
    `java-gradle-plugin`
    `kotlin-dsl`
    `project-report`
}
LicenseReporter.generateReportIn(project)

dependencies {
    implementation(project(":gradle-plugin-api"))
}

gradlePlugin {
    plugins {
        val packageName = "io.spine.tools.gradle.lib.given"
        create("stubPlugin") {
            id = "io.spine.test.stub"
            implementationClass = "$packageName.StubPlugin"
        }
        create("anotherStubPlugin") {
            id = "io.spine.test.another-stub"
            implementationClass = "$packageName.AnotherStubPlugin"
        }
        create("stubSettingPlugin") {
            id = "io.spine.test.settings"
            implementationClass = "$packageName.StubSettingsPlugin"
        }
    }
}

publishing {
    // Do nothing. We do not publish these test fixtures.
}
