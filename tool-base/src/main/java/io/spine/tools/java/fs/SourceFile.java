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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.spine.code.fs.AbstractSourceFile;
import io.spine.code.java.ClassName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.type.Type;

import java.nio.file.Path;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.java.fs.JavaFiles.resolve;
import static io.spine.tools.java.fs.JavaFiles.toDirectory;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A a path to a Java source code file.
 */
@SuppressWarnings("unused")
public final class SourceFile extends AbstractSourceFile {

    private SourceFile(Path path) {
        super(path);
    }

    static SourceFile of(Path path) {
        checkNotNull(path);
        var result = new SourceFile(path);
        return result;
    }

    /**
     * Obtains the generated file {@link Path} for the specified type.
     *
     * @param type
     *         the type from which the file is generated
     * @return a source file with a relative path
     */
    public static SourceFile forType(Type<?, ?> type) {
        var classFile = whichDeclares(type.javaClassName());
        return classFile;
    }

    /**
     * Resolves the file which contains the declaration of the given class.
     *
     * <p>The resulting {@code SourceFile} represents a <strong>relative</strong> path
     * to the Java file starting at the top level package.
     *
     * <p>In the simplest case, the file name is the same as the simple class name.
     * However, if the class is nested, then the file name coincides with the simple
     * name of the top-level class.
     *
     * @param javaClass
     *         the name of the class to resolve
     * @return a source file (with a relative path) in which the Java class is declared
     */
    public static SourceFile whichDeclares(ClassName javaClass) {
        checkNotNull(javaClass);
        var directory = toDirectory(javaClass.packageName());
        var topLevelClass = javaClass.topLevelClass();
        var javaFile = FileName.forType(topLevelClass.value());
        var sourceFile = resolve(directory, javaFile);
        return sourceFile;
    }

    /**
     * Obtains the generated file {@link Path} for the specified file descriptor.
     *
     * @param file
     *         the proto file descriptor
     * @return a source file with a relative path
     */
    public static SourceFile forOuterClassOf(FileDescriptorProto file) {
        checkNotNull(file);
        var filename = FileName.forType(SimpleClassName.outerOf(file).value());
        var dir = getGeneratedFolder(file);
        var result = resolve(dir, filename);
        return result;
    }

    /**
     * Obtains directory that contains a generated file from the file descriptor.
     *
     * @param file
     *         the proto file descriptor
     * @return the relative folder path
     */
    private static Path getGeneratedFolder(FileDescriptorProto file) {
        checkNotNull(file);
        var packageName = PackageName.resolve(file);
        var result = toDirectory(packageName);
        return result;
    }

    /**
     * Obtains the generated file for the specified message descriptor.
     *
     * @param message
     *         the descriptor of the message type for which we obtain the source code file
     * @param file
     *         the descriptor of the proto file which contains the declaration of the
     *         message type
     * @return the source file with a relative path
     * @see #forMessage(DescriptorProto, FileDescriptorProto)
     */
    public static SourceFile forMessage(Descriptor message, FileDescriptor file) {
        return forMessage(message.toProto(), file.toProto());
    }

    /**
     * Obtains the generated file for the specified message descriptor.
     *
     * @param message
     *         the descriptor of the message type for which we obtain the source code file
     * @param file
     *         the descriptor of the proto file which contains the declaration
     *         of the message type
     * @return the source file with a relative path
     * @see #forMessage(Descriptor, FileDescriptor)
     */
    public static SourceFile forMessage(DescriptorProto message, FileDescriptorProto file) {
        return forMessageOrInterface(message, file, FileName::forMessage);
    }

    /**
     * Obtains the generated file for the {@code MessageOrBuilder} interface of
     * the specified message descriptor.
     *
     * @param message
     *         the descriptor of the message type for which we obtain the source code file
     * @param file
     *         the descriptor of the proto file which contains the declaration
     *         of the message type
     * @return the source file with a relative path
     */
    public static SourceFile forMessageOrBuilder(Descriptor message, FileDescriptor file) {
        return forMessageOrBuilder(message.toProto(), file.toProto());
    }

    /**
     * Obtains the generated file for the {@code MessageOrBuilder} interface of
     * the specified message descriptor.
     *
     * @param message
     *         the descriptor of the message type for which we obtain the source code file
     * @param file
     *         the descriptor of the proto file which contains the declaration of
     *         the message type
     * @return the source file with a relative path
     */
    public static SourceFile forMessageOrBuilder(DescriptorProto message,
                                                 FileDescriptorProto file) {
        return forMessageOrInterface(message, file, FileName::forMessageOrBuilder);
    }

    private static SourceFile forMessageOrInterface(DescriptorProto message,
                                                    FileDescriptorProto file,
                                                    Function<DescriptorProto, FileName> fileName) {
        checkNotNull(file);
        checkNotNull(message);
        var typeName = message.getName();
        if (!file.getMessageTypeList().contains(message)) {
            throw missingNestedDefinition(file.getName(), typeName);
        }
        if (file.getOptions().getJavaMultipleFiles()) {
            var filename = fileName.apply(message);
            var dir = getGeneratedFolder(file);
            var result = resolve(dir, filename);
            return result;
        } else {
            var result = forOuterClassOf(file);
            return result;
        }
    }

    private static IllegalStateException missingNestedDefinition(String file, String definition) {
        throw newIllegalStateException("`%s` does not contain nested definition `%s`.",
                                       file, definition);
    }

    /**
     * Obtains the generated file for the specified enum descriptor.
     *
     * @param enumType
     *         the enum descriptor to get the file for
     * @param file
     *         the file descriptor containing the enum descriptor
     * @return the source file with a relative path
     */
    public static SourceFile forEnum(EnumDescriptor enumType, FileDescriptor file) {
        return forEnum(enumType.toProto(), file.toProto());
    }

    /**
     * Obtains the generated file for the specified enum descriptor.
     *
     * @param enumType
     *         the enum descriptor to get the file for
     * @param file
     *         the file descriptor containing the enum descriptor
     * @return the source file with a relative path
     */
    public static SourceFile forEnum(EnumDescriptorProto enumType, FileDescriptorProto file) {
        checkNotNull(file);
        checkNotNull(enumType);
        if (!file.getEnumTypeList().contains(enumType)) {
            throw missingNestedDefinition(file.getName(), enumType.getName());
        }
        if (file.getOptions().getJavaMultipleFiles()) {
            var filename = FileName.forEnum(enumType);
            var dir = getGeneratedFolder(file);
            var result = resolve(dir, filename);
            return result;
        } else {
            var result = forOuterClassOf(file);
            return result;
        }
    }

    /**
     * Obtains the generated file for the specified service descriptor.
     *
     * @param service
     *         the service descriptor to get the file for
     * @param file
     *         the file descriptor containing the service descriptor
     * @return the source file with a relative path
     */
    public static SourceFile forService(ServiceDescriptor service, FileDescriptor file) {
        checkNotNull(service);
        checkNotNull(file);
        return forService(service.toProto(), file.toProto());
    }

    /**
     * Obtains the generated file for the specified service descriptor.
     *
     * @param service
     *         the service descriptor to get the file for
     * @param file
     *         the file descriptor containing the service descriptor
     * @return the source file with a relative path
     */
    public static SourceFile forService(ServiceDescriptorProto service, FileDescriptorProto file) {
        checkNotNull(service);
        checkNotNull(file);
        var serviceType = service.getName();
        if (!file.getServiceList().contains(service)) {
            throw missingNestedDefinition(file.getName(), serviceType);
        }
        var filename = FileName.forService(service);
        var dir = getGeneratedFolder(file);
        var result = resolve(dir, filename);
        return result;
    }

    /**
     * Obtains a Java source file with the source code of the give type in the given package.
     */
    public static SourceFile forType(String javaPackage, String typename) {
        checkNotEmptyOrBlank(javaPackage);
        checkNotEmptyOrBlank(typename);
        var packageName = PackageName.of(javaPackage);
        var directory = toDirectory(packageName);
        var result = resolve(directory, FileName.forType(typename));
        return result;
    }

    /**
     * Obtains a source file of the specified class.
     */
    public static SourceFile of(Class<?> cls) {
        checkNotNull(cls);
        var packageName = PackageName.of(cls);
        var directory = toDirectory(packageName);
        return forType(directory.toString(), cls.getSimpleName());
    }
}
