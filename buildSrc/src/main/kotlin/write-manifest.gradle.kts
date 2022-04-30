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

import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION
import java.util.jar.Attributes.Name.IMPLEMENTATION_TITLE
import java.util.jar.Attributes.Name.IMPLEMENTATION_VENDOR

plugins {
    java
}

fun prop(key: String): String = System.getProperties()[key].toString()

fun currentTime(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date())

fun buildJdk(): String =
    "${prop("java.version")} (${prop("java.vendor")} ${prop("java.vm.version")})"

fun buildOs(): String =
    "${prop("os.name")} ${prop("os.arch")} ${prop("os.version")}"

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Built-By" to prop("user.name"),
                "Build-Timestamp" to currentTime(),
                "Created-By" to "Gradle ${gradle.gradleVersion}",
                "Build-Jdk" to buildJdk(),
                "Build-OS" to buildOs(),
                IMPLEMENTATION_TITLE.toString() to "${project.group}:${project.name}",
                IMPLEMENTATION_VERSION.toString() to project.version,
                IMPLEMENTATION_VENDOR.toString() to "TeamDev"
            )
        )
    }
}

sourceSets {
    main {
        resources.srcDir("$buildDir/tmp/jar")
    }
}
