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
package io.spine.tools.java.fs

import com.google.common.testing.NullPointerTester
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.EnumDescriptor
import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.Descriptors.ServiceDescriptor
import com.google.protobuf.Empty
import io.grpc.protobuf.ProtoServiceDescriptorSupplier
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.spine.option.EntityOption
import io.spine.test.code.NoOuterClassnameSourceFileTest.NoOuterClassnameMessage
import io.spine.test.code.SourceFile.NestedMessage
import io.spine.test.code.StandaloneMessage
import io.spine.testing.setDefault
import io.spine.tools.type.ProjectServiceGrpc
import io.spine.type.MessageType
import java.nio.file.Paths
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import spine.test.code.InheritAllSourceFileTest.InheritAllMessage
import spine.test.code.InheritPackage

@DisplayName("Java `SourceFile` should point to a file which")
internal class SourceFileSpec {

    @Test
    fun checkNulls() {
        val descr = Empty.getDescriptor()
        val fileDescr = Empty.getDescriptor()
        val enumDescr = EntityOption.Kind.getDescriptor()
        NullPointerTester().run {
            setDefault("nonblank")
            setDefault<FileDescriptor>(fileDescr.file)
            setDefault<FileDescriptorProto>(fileDescr.file.toProto())
            setDefault<Descriptor>(descr)
            setDefault<DescriptorProto>(descr.toProto())
            setDefault<EnumDescriptor>(enumDescr)
            setDefault<EnumDescriptorProto>(enumDescr.toProto())
            setDefault<ServiceDescriptor>(
                (ProjectServiceGrpc.getServiceDescriptor().schemaDescriptor as
                        ProtoServiceDescriptorSupplier).serviceDescriptor
            )
            setDefault<ServiceDescriptorProto>(ServiceDescriptorProto.getDefaultInstance())
        }.testAllPublicStaticMethods(SourceFile::class.java)
    }

    private fun assertPath(messageType: Descriptor, expectedPath: String) {
        val sourceFile = SourceFile.forMessage(messageType)
        val path = Paths.get(expectedPath)

        sourceFile.path() shouldBe path
    }

    @Test
    fun `has a separate file`() {
        assertPath(
            StandaloneMessage.getDescriptor(),
            "io/spine/test/code/StandaloneMessage.java"
        )
    }

    @Nested
    @DisplayName("is of specified")
    inner class TypeParameterSpec {

        @Test
        fun type() {
            val type = MessageType.of(StandaloneMessage.getDefaultInstance())
            val sourceFile = SourceFile.forType(type)
            assertContains(sourceFile, "StandaloneMessage.java")
        }

        @Test
        fun `'Message' type`() {
            val sourceFile = SourceFile.forMessage(StandaloneMessage.getDescriptor())
            assertContains(sourceFile, "StandaloneMessage.java")
        }

        @Test
        fun `'MessageOrBuilder' type`() {
            val sourceFile = SourceFile.forMessageOrBuilder(StandaloneMessage.getDescriptor())
            assertContains(sourceFile, "StandaloneMessageOrBuilder.java")
        }

        @Test
        fun `enum type`() {
            val sourceFile = SourceFile.forEnum(EntityOption.Kind.getDescriptor())
            assertContains(sourceFile, "EntityOption.java")
        }

        @Test
        fun `service type`() {
            val serviceDescriptor = (ProjectServiceGrpc.getServiceDescriptor().schemaDescriptor as
                    ProtoServiceDescriptorSupplier).serviceDescriptor
            val sourceFile = SourceFile.forService(serviceDescriptor)
            assertContains(sourceFile, "ProjectServiceGrpc.java")
        }

        private fun assertContains(sourceFile: SourceFile, path: String) {
            sourceFile.path() shouldContain Paths.get(path)
        }
    }

    @Test
    fun `is declared in an outer class with a custom name`() {
        assertPath(
            NestedMessage.getDescriptor(),
            "io/spine/test/code/SourceFile.java"
        )
    }

    @Test
    fun `is declared in an outer class with the default name`() {
        assertPath(
            NoOuterClassnameMessage.getDescriptor(),
            "io/spine/test/code/NoOuterClassnameSourceFileTest.java"
        )
    }

    @Test
    fun `inherits Protobuf package`() {
        assertPath(
            InheritPackage.getDescriptor(),
            "spine/test/code/InheritPackage.java"
        )
    }

    @Test
    fun `inherits Protobuf package and is declared in an outer class`() {
        assertPath(
            InheritAllMessage.getDescriptor(),
            "spine/test/code/InheritAllSourceFileTest.java"
        )
    }
}
