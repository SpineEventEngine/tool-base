/*
 * Copyright 2024, TeamDev. All rights reserved.
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

plugins {
    `intellij-platform-jar`
    kotlin("jvm")
}

description = "IntelliJ Platform for Java"

val intellijPlatformModule = project(":intellij-platform")

dependencies {
    api(intellijPlatformModule)
}

/**
 * Exclude files from `intellij-platform` fat JAR when packing fat JAR for this module.
 */
tasks.shadowJar {
    val platformJarTask = intellijPlatformModule.tasks.shadowJar
    dependsOn(platformJarTask)
    val pathsToExclude = mutableListOf<String>()
    doFirst {
        // The path to the file produced for `intellij-platform` module.
        val jarPath = platformJarTask.get().archiveFile.get().asFile
        zipTree(jarPath).visit {
            if (!isDirectory) {
                pathsToExclude.add(this.path)
            }
        }
    }                                                               
    exclude {
        it.path in pathsToExclude
    }
}
