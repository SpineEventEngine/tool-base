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

package io.spine.tools.java.code;

import com.squareup.javapoet.JavaFile;
import io.spine.logging.WithLogging;
import io.spine.tools.code.Indent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;

/**
 * Writes the {@link TypeSpec} to a file.
 */
@SuppressWarnings("unused") /* Part of the public API. */
public final class TypeSpecWriter implements WithLogging {

    private final TypeSpec spec;
    private final Indent indent;

    public TypeSpecWriter(TypeSpec spec, Indent indent) {
        this.spec = spec;
        this.indent = indent;
    }

    /**
     * Writes the spec to a file.
     *
     * @param outputDir
     *         the root dir to write to
     * @implNote When passing the indentation value to JavaPoet,
     *         this method uses a nominal value of {@code indent.level()}.
     *         JavaPoet library then utilizes this value to calculate
     *         the indentation level for each line in the generated code.
     */
    public void write(Path outputDir) {
        try {
            logger().atDebug().log(() -> format("Creating the output directory `%s`.", outputDir));
            Files.createDirectories(outputDir);

            var typeSpec = this.spec.toPoet();
            var className = typeSpec.name;
            logger().atDebug().log(() -> format("Writing `%s.java`.", className));

            var packageName = this.spec.packageName().value();
            var indentLevel = indent.at(1).toString();
            var javaFile = JavaFile.builder(packageName, typeSpec)
                    .skipJavaLangImports(true)
                    .indent(indentLevel)
                    .build();
            javaFile.writeTo(outputDir);
            logger().atDebug().log(() -> format("File `%s.java` written successfully.", className));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
