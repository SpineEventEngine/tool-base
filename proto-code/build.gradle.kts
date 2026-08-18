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

import io.spine.dependency.lib.Grpc
import io.spine.dependency.lib.GrpcKotlin
import io.spine.dependency.lib.Protobuf
import io.spine.dependency.local.Base
import io.spine.dependency.local.Logging

plugins {
    module
    protobuf
    id("io.spine.descriptor-set-file")
    id("io.spine.generated-sources")
}

description = "Protobuf-specific types for code generation, descriptor sets, and file layout"

dependencies {
    api(Base.lib)

    // `io.spine.tools.proto.fs.Directory` extends `SourceCodeDirectory`
    // and accepts an `AbstractDirectory` as its parent.
    api(project(":fs"))

    // `FileDescriptorSuperset` reads descriptor sets out of archives using
    // `io.spine.tools.archive`. The types stay inside its implementation.
    implementation(project(":tool-base"))

    implementation(Logging.lib)?.because("`FileDescriptorSuperset` is `WithLogging`.")

    // `DirectorySpec` needs a concrete `AbstractDirectory` to parent the `proto` root.
    testImplementation(project(":java-code"))

    // `FileDescriptorSupersetTest` builds descriptor sets from the `PersonProto`,
    // `ProjectProto` and `TaskProto` fixtures of `tool-base`, which generate into
    // `io.spine.tools.type` — the same package, hence no import to give them away.
    // The tests of `tool-base` and `java-code` share these fixtures.
    testImplementation(testFixtures(project(":tool-base")))

    // Those fixtures carry gRPC-generated code, whose versions `tool-base`
    // supplies through the resolution strategy below rather than through
    // the coordinates themselves.
    listOf(
        Grpc.protobuf,
        Grpc.core,
        Grpc.stub,
        GrpcKotlin.stub,
    ).forEach {
        testImplementation(it)
    }
}

configurations {
    all {
        resolutionStrategy {
            Grpc.forceArtifacts(project, this@all, this@resolutionStrategy)
        }
    }
}

protobuf {
    protoc {
        artifact = Protobuf.compiler
    }
}

allowDuplicationInSourcesJar()
