/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.java.js;

import io.spine.tools.java.fs.DefaultJavaPaths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.google.common.truth.Truth8.assertThat;
import static io.spine.tools.code.SourceSetName.main;
import static io.spine.tools.code.SourceSetName.test;

@DisplayName("`DefaultJavaPaths` should")
@SuppressWarnings("DuplicateStringLiteralInspection")
class DefaultJavaPathsTest {

    private static final Path projectPath = Path.of("/test-path");

    @Test
    @DisplayName("obtain `build` dir")
    void build() {
        var paths = DefaultJavaPaths.at(projectPath);
        assertThat(paths.buildRoot()
                        .path())
                .isEqualTo(projectPath.resolve("build"));
    }

    @Test
    @DisplayName("obtain `generated` dir")
    void generated() {
        var paths = DefaultJavaPaths.at(projectPath);
        assertThat(paths.generated()
                        .path())
                .isEqualTo(projectPath.resolve("generated"));
    }

    @Test
    @DisplayName("obtain `java` subdir in `generated-proto` dir")
    void generatedJava() {
        var paths = DefaultJavaPaths.at(projectPath);
        assertThat(paths.generatedProto()
                        .java(main)
                        .path())
                .isEqualTo(projectPath.resolve("build")
                                      .resolve("generated-proto")
                                      .resolve("main")
                                      .resolve("java"));
    }

    @Test
    @DisplayName("obtain `spine` subdir in `generated-proto` dir")
    void generatedSpine() {
        var paths = DefaultJavaPaths.at(projectPath);
        assertThat(paths.generatedProto()
                        .spine(test)
                        .path())
                .isEqualTo(projectPath.resolve("build")
                                      .resolve("generated-proto")
                                      .resolve("test")
                                      .resolve("spine"));
    }

    @Test
    @DisplayName("obtain `grpc` subdir in `generated-proto` dir")
    void generatedGrpc() {
        var paths = DefaultJavaPaths.at(projectPath);
        assertThat(paths.generatedProto()
                        .grpc(test)
                        .path())
                .isEqualTo(projectPath.resolve("build")
                                      .resolve("generated-proto")
                                      .resolve("test")
                                      .resolve("grpc"));
    }
}
