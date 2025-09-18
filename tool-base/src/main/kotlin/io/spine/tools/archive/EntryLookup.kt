/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.tools.archive

import com.google.common.collect.ImmutableSet
import com.google.common.io.ByteStreams
import io.spine.logging.WithLogging
import io.spine.util.Exceptions
import java.io.Closeable
import java.io.IOException
import java.util.zip.ZipInputStream

/**
 * A process of an entry lookup inside an archive.
 */
internal class EntryLookup private constructor(
    private val stream: ZipInputStream
) : Closeable, WithLogging {

    /**
     * Finds an entry with the given name in the archive.
     *
     *
     * This method should only be called once in the lifetime of an `EntryLookup`.
     * All subsequent calls will always return an empty result.
     *
     * @param fileExtension The name of the entry in terms of `ZipEntry.getName()`.
     * @return a snapshot of the found entry or `Optional.empty()` if there is no such an entry.
     */
    fun findByExtension(fileExtension: String): MutableCollection<ArchiveEntry> {
        try {
            return doFindEntry(fileExtension)
        } catch (e: IOException) {
            throw Exceptions.illegalStateWithCauseOf(e)
        }
    }

    @Throws(IOException::class)
    private fun doFindEntry(fileExtension: String): MutableCollection<ArchiveEntry> {
        val result = ImmutableSet.builder<ArchiveEntry>()
        var entry = stream.getNextEntry()
        while (entry != null) {
            val entryName = entry.getName()
            if (entryName.endsWith(fileExtension)) {
                atDebug.log { "Reading ZIP entry `$entryName`." }
                val read = readEntry()
                result.add(read)
            }
            entry = stream.getNextEntry()
        }
        return result.build()
    }

    @Throws(IOException::class)
    private fun readEntry(): ArchiveEntry {
        // When being read, a `ZipStream` reports to be out of data when the *current entry* is
        // over, not when the whole archive is read.
        val bytes = ByteStreams.toByteArray(stream)
        return ArchiveEntry.of(bytes)
    }

    /**
     * Closes the underlying [ZipInputStream].
     *
     * @throws IOException if `InputStream.close()` throws an `IOException`.
     */
    @Throws(IOException::class)
    override fun close() {
        stream.close()
    }

    companion object {

        /**
         * Opens the given archive for a lookup.
         *
         * @return a new instance of `EntryLookup`
         */
        @JvmStatic
        fun open(archiveFile: ArchiveFile): EntryLookup {
            val stream = ZipInputStream(archiveFile.open())
            return EntryLookup(stream)
        }
    }
}
