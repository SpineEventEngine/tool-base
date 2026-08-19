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

@file:Suppress("RemoveRedundantQualifierName")

import com.google.protobuf.gradle.id
import io.spine.dependency.lib.Grpc
import io.spine.dependency.lib.GrpcKotlin
import io.spine.dependency.lib.Protobuf
import io.spine.dependency.local.Base

/**
 * Protobuf fixtures for the tests of *this* repository — and nothing else.
 *
 * The types generated here exist to give the tests of other modules real
 * descriptors to work on: file layouts with and without `java_outer_classname`
 * or `java_package`, nested messages, and a service. They are consumed only as
 * `testImplementation(project(":fixtures"))`.
 *
 * This module is deliberately **not published**. The root `build.gradle.kts`
 * excludes it from `spinePublishing`, so no downstream project can depend on
 * these declarations, and they can be renamed or deleted whenever the tests
 * that use them change. Anything needed outside this repository belongs in
 * a published module instead.
 *
 * The Protobuf packages live under `spine.test.*` for the same reason: the
 * namespace marks them as fixtures and keeps them clear of the production
 * packages the other modules own.
 */
plugins {
    module
    protobuf
    id("io.spine.descriptor-set-file")
    id("io.spine.generated-sources")
}

description = "Protobuf fixtures for the tests of this repository. Not published."

configurations {
    all {
        resolutionStrategy {
            Grpc.forceArtifacts(project, this@all, this@resolutionStrategy)
        }
    }
}

dependencies {
    api(Base.lib)

    // `project.proto` declares a service, so the generated code — and every
    // test compiling against it — needs the gRPC runtime.
    listOf(
        Grpc.protobuf,
        Grpc.core,
        Grpc.stub,
        GrpcKotlin.stub,
    ).forEach {
        api(it)
    }
}

sourceSets {
    main {
        java.srcDirs("$projectDir/generated/main/grpc")
    }
}

protobuf {
    protoc {
        artifact = Protobuf.compiler
    }

    plugins {
        Grpc.ProtocPlugin.let {
            id(it.id) { artifact = it.artifact }
        }
        GrpcKotlin.ProtocPlugin.let {
            id(it.id) { artifact = it.artifact }
        }
    }

    generateProtoTasks.all().configureEach {
        builtins.maybeCreate("kotlin")
        plugins {
            id(Grpc.ProtocPlugin.id)
            id(GrpcKotlin.ProtocPlugin.id)
        }
    }
}

allowDuplicationInSourcesJar()
