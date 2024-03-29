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

package io.spine.tools.fs;

import io.spine.annotation.Internal;

/**
 * Enumerates default names for project structure directories.
 *
 * <p>The purpose of this enumeration is to use the {@link #value()} method
 * of its members instead of string constants.
 */
public enum DirectoryName {

    /** Contains Java source code. */
    java,

    /** Contains JavaScript source code. */
    js,

    /** Contains Dart source code. */
    dart,

    /** Contains Kotlin source code. */
    kotlin,

    /** Root directory for source code. */
    src,

    /** Contains production code. */
    main,

    /** Contains test code. */
    test,

    /** Contains program resources. */
    resources,

    /**
     * The root directory of the generated code.
     *
     * <p>Contains {@link #main} and {@link #test}.
     */
    generated,

    /**
     * The root directory for the intermediate generated code used by ProtoData.
     *
     * @deprecated Please use {@link #generated} instead.
     */
    @Deprecated
    generatedProto("generated-proto"),

    /** Spine-specific generated code. */
    spine,

    /** Code generated by gRPC. */
    grpc,

    /** Contains build results. */
    build,

    /** Contains Protobuf descriptor set files produced during the build. */
    descriptors,

    /**
     * Internal directory name for storing temporary build artifacts.
     *
     * @see DefaultPaths#tempArtifacts()
     */
    @Internal
    dotSpine(".spine");

    private final String value;

    DirectoryName() {
        this.value = name();
    }

    DirectoryName(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
