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

package io.spine.tools.java.code.field;

import com.squareup.javapoet.CodeBlock;
import io.spine.tools.java.code.GeneratedJavadoc;
import io.spine.code.proto.FieldDeclaration;

/**
 * The Javadoc of a method which returns a strongly-typed proto field.
 *
 * @see FieldAccessor
 */
final class FieldAccessorDoc {

    /** Prevents instantiation of this class. */
    private FieldAccessorDoc() {
    }

    static GeneratedJavadoc generateFor(FieldDeclaration field) {
        return GeneratedJavadoc.twoParagraph(
                CodeBlock.of("Returns the $L$S field.", fieldKind(field), field.name()),
                CodeBlock.of("The $L Java type is {@code $L}.", elementDescribedByType(field),
                             field.javaTypeName())
        );
    }

    private static String fieldKind(FieldDeclaration field) {
        if (field.isRepeated()) {
            return "{@code repeated} ";
        }
        if (field.isMap()) {
            return "{@code map} ";
        }
        return "";
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    private static String elementDescribedByType(FieldDeclaration field) {
        if (field.isRepeated()) {
            return "element";
        }
        if (field.isMap()) {
            return "value";
        }
        return "field";
    }
}
