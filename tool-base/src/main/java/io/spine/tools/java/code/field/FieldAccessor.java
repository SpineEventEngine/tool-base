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
import com.squareup.javapoet.MethodSpec;
import io.spine.base.SubscribableField;
import io.spine.tools.java.code.GeneratedJavadoc;
import io.spine.tools.java.code.GeneratedMethodSpec;
import io.spine.tools.java.code.JavaPoetName;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;

import javax.lang.model.element.Modifier;

import static com.google.common.base.Preconditions.checkState;

/**
 * A spec of the method which returns a {@linkplain SubscribableField strongly-typed message field}.
 *
 * <p>The name of the method matches the field name in {@code javaCase}.
 *
 * <p>The descendants of this class differentiate between top-level and nested fields to enable the
 * correct field path propagation.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
// Random duplication of some generated code elements.
abstract class FieldAccessor implements GeneratedMethodSpec {

    private final FieldDeclaration field;
    private final ClassName fieldSupertype;

    FieldAccessor(FieldDeclaration field, ClassName fieldSupertype) {
        this.field = field;
        this.fieldSupertype = fieldSupertype;
    }

    @Override
    public MethodSpec methodSpec() {
        var result = MethodSpec.methodBuilder(fieldName().javaCase())
                .addJavadoc(javadoc().spec())
                .addModifiers(modifiers())
                .returns(returnType().value())
                .addStatement(methodBody())
                .build();
        return result;
    }

    /**
     * Returns the field name as defined in Protobuf.
     */
    FieldName fieldName() {
        return field.name();
    }

    /**
     * Returns the modifiers which are applied to the generated method.
     */
    abstract Iterable<Modifier> modifiers();

    /**
     * Obtains the method return type.
     */
    JavaPoetName returnType() {
        return shouldExposeNestedFields()
               ? nestedFieldsContainer()
               : simpleField();
    }

    /**
     * Obtains the method body.
     */
    abstract CodeBlock methodBody();

    /**
     * Checks if the wrapped field has nested fields and should expose them to subscribers.
     */
    private boolean shouldExposeNestedFields() {
        return field.isSingularMessage();
    }

    /**
     * Obtains a JavaPoet name for the type representing a nested field container which is
     * returned from this method.
     */
    private JavaPoetName nestedFieldsContainer() {
        var type = JavaPoetName.of(fieldTypeName().with("Field"));
        return type;
    }

    /**
     * Obtains a JavaPoet name for the simple field (i.e. the one which doesn't expose nested
     * ones) returned by this method.
     */
    private JavaPoetName simpleField() {
        var type = JavaPoetName.of(fieldSupertype);
        return type;
    }

    /**
     * A simple name of the field type.
     *
     * <p>Assumes the wrapped field is a {@link com.google.protobuf.Message Message}.
     */
    private SimpleClassName fieldTypeName() {
        checkState(field.isMessage());
        var fieldTypeName = field.javaTypeName();
        var result = ClassName.of(fieldTypeName).toSimple();
        return result;
    }

    /**
     * Generates the method Javadoc.
     */
    private GeneratedJavadoc javadoc() {
        return FieldAccessorDoc.generateFor(this.field);
    }
}
