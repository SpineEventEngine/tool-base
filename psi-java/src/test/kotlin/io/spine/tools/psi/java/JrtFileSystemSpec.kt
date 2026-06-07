/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.psi.java

import com.intellij.util.io.URLUtil.JAR_SEPARATOR
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`JrtFileSystem` should")
internal class JrtFileSystemSpec {

    private val fileSystem = JrtFileSystem()
    private val jdkHome: String = System.getProperty("java.home")

    private fun jrtPath(inImage: String) = "$jdkHome$JAR_SEPARATOR$inImage"

    @Test
    fun `expose the JRT protocol`() {
        fileSystem.protocol shouldBe "jrt"
    }

    @Test
    fun `split a path into the JDK home and the in-image path`() {
        val (home, inImage) = JrtFileSystem.splitPath(jrtPath("java.base/module-info.class"))

        home shouldBe jdkHome
        inImage shouldBe "java.base/module-info.class"
    }

    @Test
    fun `reject a path without a separator`() {
        assertThrows<IllegalStateException> {
            JrtFileSystem.splitPath("no-separator-here")
        }
    }

    @Test
    fun `return 'null' for a non-existing JDK home`() {
        fileSystem.findFileByPath("/no/such/jdk${JAR_SEPARATOR}java.base") shouldBe null
    }

    @Test
    fun `find the root of the image`() {
        val root = fileSystem.findFileByPath(jrtPath(""))

        root.shouldNotBeNull()
        root.let {
            it.isDirectory shouldBe true
            it.isWritable shouldBe false
            it.isValid shouldBe true
            it.parent shouldBe null
            it.fileSystem shouldBe fileSystem
            it.children.isNotEmpty() shouldBe true
        }
    }

    @Test
    fun `refresh without effect and find the file again`() {
        fileSystem.refresh(false)
        val found = fileSystem.refreshAndFindFileByPath(jrtPath(""))
        found.shouldNotBeNull()
    }

    @Test
    fun `clear the cached roots`() {
        fileSystem.findFileByPath(jrtPath("")).shouldNotBeNull()
        fileSystem.clearRoots()
        fileSystem.findFileByPath(jrtPath("")).shouldNotBeNull()
    }

    @Test
    fun `expose a class file as a virtual file`() {
        val classFile = fileSystem.findFileByPath(jrtPath("modules/java.base/module-info.class"))

        classFile.shouldNotBeNull()
        classFile.let {
            it.name shouldBe "module-info.class"
            it.path shouldContain "java.base"
            it.isDirectory shouldBe false
            it.parent.shouldNotBeNull()
            (it.length > 0) shouldBe true
            (it.timeStamp >= 0) shouldBe true
            it.modificationStamp shouldBe 0
            it.contentsToByteArray().isNotEmpty() shouldBe true
            (it.inputStream.use { stream -> stream.read() } >= 0) shouldBe true
        }
    }

    @Test
    fun `reject writing to a virtual file`() {
        val classFile = fileSystem.findFileByPath(jrtPath("modules/java.base/module-info.class"))
        classFile.shouldNotBeNull()

        assertThrows<UnsupportedOperationException> {
            classFile.getOutputStream(this, 0L, 0L)
        }
    }

    @Test
    fun `refresh a virtual file without effect`() {
        val classFile = fileSystem.findFileByPath(jrtPath("modules/java.base/module-info.class"))
        classFile.shouldNotBeNull()

        classFile.refresh(false, false, null)
        classFile.isValid shouldBe true
    }

    @Test
    fun `support equality of virtual files`() {
        val first = fileSystem.findFileByPath(jrtPath("modules/java.base/module-info.class"))
        val second = fileSystem.findFileByPath(jrtPath("modules/java.base/module-info.class"))

        first.shouldNotBeNull()
        second.shouldNotBeNull()
        (first == second) shouldBe true
        first.hashCode() shouldBe second.hashCode()
    }
}
