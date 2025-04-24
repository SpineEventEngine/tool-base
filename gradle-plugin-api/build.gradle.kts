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

import io.spine.dependency.lib.Jackson
import io.spine.dependency.lib.Protobuf

plugins {
    module
}

/**
 * The project containing two stub plugins used by the tests of this module.
 */
val pluginTestFixturesProject = project(":gradle-plugin-api-test-fixtures")

dependencies {
    val rootPluginProject = project(":gradle-root-plugin")
    api(rootPluginProject)
    Protobuf.libs.forEach {
        api(it)?.because("""
            We need the `Message` interface for conversion of compilation settings that
            would be passed to Spine Compiler plugins.
            """.trimIndent()
        )
    }
    implementation(Jackson.DataFormat.yamlArtifact)

    testImplementation(project(":plugin-base"))
    testImplementation(project(":plugin-testlib"))
    testImplementation(pluginTestFixturesProject)
}

/**
 * This task copies the directory `build/pluginUnderTestMetadata/` from
 * the `pluginTestFixturesProject` project into the `build` directory of this project
 * so that `GradleRunner` used by `LibrarySettingsPluginSpec` test can pick up the metadata file.
 *
 * We do it in two steps:
 *  1. Copy the directory under the build, which is done by this task.
 *  2. Add the copied resources to the test resources by `processTestResources` task.
 *
 * Two steps make the copied resource directory more visible under the `build` directory.
 */
val copyPluginMetadata = tasks.register<Copy>("copyPluginMetadata") {
    val dirName = "pluginUnderTestMetadata"
    from(pluginTestFixturesProject.layout.buildDirectory.dir(dirName))
    into(layout.buildDirectory.dir(dirName))
    // Make sure we have the resource file ready.
    dependsOn(pluginTestFixturesProject.tasks.build)
}

/**
 * Make sure the test classpath contains the property file
 * from the `pluginUnderTestMetadata` directory copied by
 * the `copyPluginMetadata` task.
 */
tasks {
    processTestResources {
        from(copyPluginMetadata)
    }
    test {
        dependsOn(copyPluginMetadata)
    }
}
