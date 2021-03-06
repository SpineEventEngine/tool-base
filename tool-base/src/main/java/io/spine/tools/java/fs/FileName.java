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

package io.spine.tools.java.fs;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import io.spine.code.fs.AbstractFileName;
import io.spine.code.java.SimpleClassName;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A Java file name.
 */
@Immutable
public final class FileName extends AbstractFileName<FileName> {

    private static final long serialVersionUID = 0L;
    private static final String GRPC_CLASSNAME_SUFFIX = "Grpc";
    private static final String EXTENSION = ".java";

    private FileName(String value) {
        super(value);
    }

    /**
     * Obtains the name for the passed type.
     */
    public static FileName forType(String typeName) {
        checkNotEmptyOrBlank(typeName);
        var result = new FileName(typeName + EXTENSION);
        return result;
    }

    /**
     * Obtains file name for the specified message.
     *
     * @param message
     *        a descriptor of the message
     * @return the name of the Java file
     */
    public static FileName forMessage(DescriptorProto message) {
        checkNotNull(message);
        var typeName = message.getName();
        var result = forType(typeName);
        return result;
    }

    /**
     * Obtains file name for the specified message {@code MessageOrBuilder} interface.
     *
     * @param message
     *        a descriptor of the message
     * @return the name of the Java file
     */
    public static FileName forMessageOrBuilder(DescriptorProto message) {
        checkNotNull(message);
        var typeName = message.getName();
        var javaType = SimpleClassName.messageOrBuilder(typeName).value();
        var result = forType(javaType);
        return result;
    }

    /**
     * Obtains file name for the passed enum.
     */
    public static FileName forEnum(EnumDescriptorProto enumType) {
        checkNotNull(enumType);
        return forType(enumType.getName());
    }

    /**
     * Obtains file name for the specified service.
     */
    public static FileName forService(ServiceDescriptorProto service) {
        checkNotNull(service);
        return forType(service.getName() + GRPC_CLASSNAME_SUFFIX);
    }

    /**
     * Checks the file path.
     *
     * @param path  the target file path
     * @return {@code true} in case if the file has the .java extension.
     */
    @SuppressWarnings("unused") /* Part of the public API. */
    public static boolean isJava(Path path) {
        checkNotNull(path);
        return path.toString()
                   .endsWith(EXTENSION);
    }
}
