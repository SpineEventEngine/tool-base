/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.tools.psi.java

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.DeprecatedVirtualFileSystem
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.util.containers.ConcurrentFactoryMap.createMap
import com.intellij.util.io.URLUtil
import com.intellij.util.lang.JavaVersion
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.FileSystem
import java.nio.file.FileSystems.newFileSystem
import java.nio.file.Files
import java.nio.file.Files.readAttributes
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

/**
 * The implementation of [VirtualFileSystem] used by [PsiJavaAppEnvironment].
 *
 * The inheritance from [DeprecatedVirtualFileSystem] may look strange, but it is
 * the option used by Kotlin compiler too because the "New" virtual file system API is
 * available in the full version of IntelliJ IDEA only.
 *
 * Original code is in `org.jetbrains.kotlin.cli.jvm.modules.CoreJrtFileSystem`.
 */
internal class JrtFileSystem : DeprecatedVirtualFileSystem() {

    private val roots =
        createMap<String, JrtVirtualFile?> { jdkHomePath ->
            val fileSystem = globalJrtFsCache[jdkHomePath] ?: return@createMap null
            JrtVirtualFile(this, jdkHomePath, fileSystem.getPath(""), parent = null)
        }

    override fun getProtocol(): String = StandardFileSystems.JRT_PROTOCOL

    @Suppress("ReturnCount") // Early return is used.
    override fun findFileByPath(path: String): VirtualFile? {
        val (jdkHomePath, pathInImage) = splitPath(path)
        val root = roots[jdkHomePath] ?: return null

        if (pathInImage.isEmpty()) return root

        return root.findFileByRelativePath(pathInImage)
    }

    override fun refresh(asynchronous: Boolean) = Unit

    override fun refreshAndFindFileByPath(path: String): VirtualFile? = findFileByPath(path)

    @Suppress("unused") // reserved for future use.
    fun clearRoots() {
        roots.clear()
    }

    companion object {
        private fun loadJrtFsJar(jdkHome: File): File? =
            File(jdkHome, "lib/jrt-fs.jar").takeIf(File::exists)

        fun splitPath(path: String): Pair<String, String> {
            val separator = path.indexOf(URLUtil.JAR_SEPARATOR)
            if (separator < 0) {
                error("Path in CoreJrtFileSystem must contain a separator: $path")
            }
            val localPath = path.substring(0, separator)
            val pathInJar = path.substring(separator + URLUtil.JAR_SEPARATOR.length)
            return Pair(localPath, pathInJar)
        }

        private val globalJrtFsCache = createMap<String, FileSystem?> { jdkHomePath ->
            val jdkHome = File(jdkHomePath)
            val jrtFsJar = loadJrtFsJar(jdkHome) ?: return@createMap null
            val rootUri = URI.create(StandardFileSystems.JRT_PROTOCOL + ":/")
            /*
              The `ClassLoader`, that was used to load JRT FS Provider actually lives as long as
              the current thread due to ThreadLocal leak in jrt-fs,
              See https://bugs.openjdk.java.net/browse/JDK-8260621
              So that cache allows us to avoid creating too many classloaders for same
              JDK and reduce severity of that leak
            */
            if (isAtLeastJava9()) {
                // If the runtime JDK is set to 9+ it has JrtFileSystemProvider,
                // but to load proper jrt-fs (one that is pointed by jdkHome), we should
                // provide `"java.home"` path.
                newFileSystem(rootUri, mapOf("java.home" to jdkHome.absolutePath))
            } else {
                val classLoader = URLClassLoader(arrayOf(jrtFsJar.toURI().toURL()), null)
                // If the runtime JDK is set to <9, there are no JrtFileSystemProvider,
                // we should create `Classloader` with `jrt-fs.jar`, and DO NOT NEED to pass
                // `"java.home" path`, as otherwise it will incur additional classloader creation
                newFileSystem(rootUri, emptyMap<String, Nothing>(), classLoader)
            }
        }
    }
}

@Suppress("TooManyFunctions") // Implementing `VirtualFile` requires it.
private class JrtVirtualFile(
    private val fileSystem: JrtFileSystem,
    private val jdkHomePath: String,
    private val path: Path,
    private val parent: JrtVirtualFile?,
) : VirtualFile() {

    private val attributes: BasicFileAttributes
        get() = readAttributes(path, BasicFileAttributes::class.java)

    override fun getFileSystem(): VirtualFileSystem = fileSystem

    override fun getName(): String =
        path.fileName.toString()

    override fun getPath(): String =
        FileUtil.toSystemIndependentName(jdkHomePath + URLUtil.JAR_SEPARATOR + path)

    override fun isWritable(): Boolean = false

    override fun isDirectory(): Boolean = Files.isDirectory(path)

    override fun isValid(): Boolean = true

    override fun getParent(): VirtualFile? = parent

    private val myChildren by lazy { computeChildren() }

    override fun getChildren(): Array<out VirtualFile> = myChildren

    private fun computeChildren(): Array<out VirtualFile> {
        val paths = try {
            Files.newDirectoryStream(path).use(Iterable<Path>::toList)
        } catch (_: IOException) {
            emptyList()
        }
        return when {
            paths.isEmpty() -> EMPTY_ARRAY
            else -> paths.map { path ->
                JrtVirtualFile(
                    fileSystem,
                    jdkHomePath,
                    path,
                    parent = this
                )
            }.toTypedArray()
        }
    }

    override fun getOutputStream(
        requestor: Any,
        newModificationStamp: Long,
        newTimeStamp: Long
    ): OutputStream =
        throw UnsupportedOperationException()

    override fun contentsToByteArray(): ByteArray =
        Files.readAllBytes(path)

    override fun getTimeStamp(): Long =
        attributes.lastModifiedTime().toMillis()

    override fun getLength(): Long = attributes.size()

    override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) = Unit

    override fun getInputStream(): InputStream =
        VfsUtilCore.inputStreamSkippingBOM(Files.newInputStream(path).buffered(), this)

    override fun getModificationStamp(): Long = 0

    override fun equals(other: Any?): Boolean =
        other is JrtVirtualFile && path == other.path && fileSystem == other.fileSystem

    override fun hashCode(): Int =
        path.hashCode()

    companion object {
        @Suppress("ConstPropertyName", "MagicNumber")
        private const val serialVersionUID: Long = 0L
    }
}

@Suppress("MagicNumber") // Java version numbers are magic.
private fun isAtLeastJava9(): Boolean {
    return JavaVersion.current() >= JavaVersion.compose(9)
}
