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
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.EnumDescriptor
import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.Descriptors.ServiceDescriptor
import com.google.protobuf.Empty
import io.spine.option.EntityOption
import io.spine.testing.setDefault
import io.spine.test.code.NoOuterClassnameSourceFileTest.NoOuterClassnameMessage
import io.spine.test.code.SourceFile.NestedMessage
import io.spine.test.code.StandaloneMessage
import java.nio.file.Paths
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import spine.test.code.InheritAllSourceFileTest.InheritAllMessage
import spine.test.code.InheritPackage

@DisplayName("Java `SourceFile` should point to a generated message class which")
internal class SourceFileSpec {

    private fun checkPath(expectedName: String, descriptor: Descriptor) {
        val file = descriptor.file
        val sourceFile = SourceFile.forMessage(descriptor, file)
        val expectedPath = Paths.get(expectedName)

        assertThat(sourceFile.path()).isEqualTo(expectedPath)
    }

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
            //.setDefault(ServiceDescriptor::class.java, ProjectApi.ge)
            //.setDefault(ServiceDescriptorProto::class.java, ServiceDescriptor.getDefaultInstance())
        }.testAllPublicStaticMethods(SourceFile::class.java)
    }

    @Test
    fun `has a separate file`() {
        checkPath(
            "io/spine/test/code/StandaloneMessage.java",
            StandaloneMessage.getDescriptor()
        )
    }

    @Test
    fun `is declared in an outer class with a custom name`() {
        checkPath(
            "io/spine/test/code/SourceFile.java",
            NestedMessage.getDescriptor()
        )
    }

    @Test
    fun `is declared in an outer class with the default name`() {
        checkPath(
            "io/spine/test/code/NoOuterClassnameSourceFileTest.java",
            NoOuterClassnameMessage.getDescriptor()
        )
    }

    @Test
    fun `inherits Protobuf package`() {
        checkPath(
            "spine/test/code/InheritPackage.java",
            InheritPackage.getDescriptor()
        )
    }

    @Test
    fun `inherits Protobuf package and is declared in an outer class`() {
        checkPath(
            "spine/test/code/InheritAllSourceFileTest.java",
            InheritAllMessage.getDescriptor()
        )
    }
}
