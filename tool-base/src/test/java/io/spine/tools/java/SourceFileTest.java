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

package io.spine.tools.java;

import com.google.protobuf.Descriptors.Descriptor;
import io.spine.test.code.NoOuterClassnameSourceFileTest.NoOuterClassnameMessage;
import io.spine.test.code.SourceFile.NestedMessage;
import io.spine.test.code.StandaloneMessage;
import io.spine.tools.java.fs.SourceFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import spine.test.code.InheritAllSourceFileTest.InheritAllMessage;
import spine.test.code.InheritPackage;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Java `SourceFile` should")
@SuppressWarnings("InnerClassMayBeStatic") // Nested test suite.
class SourceFileTest {

    @DisplayName("compile path to class generated from message which")
    @Nested
    class GeneratedMessage {

        @Test
        @DisplayName("has a separate file")
        void separate() {
            checkPath("io/spine/test/code/StandaloneMessage.java",
                      StandaloneMessage.getDescriptor());
        }

        @Test
        @DisplayName("is declared in an outer class with a custom name")
        void nested() {
            checkPath("io/spine/test/code/SourceFile.java",
                      NestedMessage.getDescriptor());
        }

        @Test
        @DisplayName("is declared in an outer class with the default name")
        void nestedInDefault() {
            checkPath("io/spine/test/code/NoOuterClassnameSourceFileTest.java",
                      NoOuterClassnameMessage.getDescriptor());
        }

        @Test
        @DisplayName("inherits Protobuf package")
        void noPackage() {
            checkPath("spine/test/code/InheritPackage.java",
                      InheritPackage.getDescriptor());
        }

        @Test
        @DisplayName("inherits Protobuf package and is declared in an outer class")
        void inheritAll() {
            checkPath("spine/test/code/InheritAllSourceFileTest.java",
                      InheritAllMessage.getDescriptor());
        }
    }

    private static void checkPath(String expectedName, Descriptor descriptor) {
        var message = descriptor.toProto();

        var file = descriptor.getFile().toProto();
        var sourceFile = SourceFile.forMessage(message, file);
        var expectedPath = Paths.get(expectedName);
        assertEquals(expectedPath, sourceFile.path());
    }
}
