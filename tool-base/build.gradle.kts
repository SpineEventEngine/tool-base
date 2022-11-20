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

@file:Suppress("RemoveRedundantQualifierName")

import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf

import io.spine.internal.dependency.Grpc
import io.spine.internal.dependency.JavaPoet
import io.spine.internal.dependency.JavaX
import io.spine.internal.dependency.Spine

buildscript {
    standardSpineSdkRepositories()
    dependencies {
        classpath(io.spine.internal.dependency.Spine.McJava.pluginLib)
    }
}

plugins {
    `java-test-fixtures`
    id(mcJava.pluginId)
    `detekt-code-analysis`
}

dependencies {
    api(JavaPoet.lib)
    api(JavaX.annotations)

    val spine = Spine(project)
    api(spine.base)
    implementation(spine.validation.runtime)

    listOf(
        Grpc.protobuf,
        Grpc.core,
        Grpc.stub,
        spine.validation.runtime
    ).forEach {
        testImplementation(it)
        testFixturesImplementation(it)
    }

    testImplementation(spine.testlib)
}

sourceSets {
    testFixtures {
        java.srcDirs("$projectDir/generated/testFixtures/grpc")
    }
}

/**
 * Force `generated` directory and Kotlin code generation.
 */
protobuf {
    generatedFilesBaseDir = "$projectDir/generated"
    generateProtoTasks {
        for (task in all()) {
            task.builtins.maybeCreate("kotlin")
        }
    }
}
