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

package io.spine.tools.archive;

import com.google.common.collect.ImmutableSet;
import io.spine.logging.Logger;
import io.spine.logging.LoggingFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipInputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.ByteStreams.toByteArray;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.lang.String.format;

/**
 * A process of an entry lookup inside an archive.
 */
final class EntryLookup implements Closeable {

    private static final Logger<?> logger = LoggingFactory.forEnclosingClass();
    private final ZipInputStream stream;

    private EntryLookup(ZipInputStream stream) {
        this.stream = stream;
    }

    /**
     * Opens the given archive for a lookup.
     *
     * @return new instance of {@code EntryLookup}
     */
    static EntryLookup open(ArchiveFile archiveFile) {
        checkNotNull(archiveFile);
        var stream = new ZipInputStream(archiveFile.open());
        return new EntryLookup(stream);
    }

    /**
     * Finds an entry with the given name in the archive.
     *
     * <p>This method should only be called once in the lifetime of an {@code EntryLookup}.
     * All subsequent calls will always return an empty result.
     *
     * @param fileExtension
     *         the name of the entry in terms of {@code ZipEntry.getName()}
     * @return a snapshot of the found entry or {@code Optional.empty()} if there is no such entry
     */
    Collection<ArchiveEntry> findByExtension(String fileExtension) {
        try {
            return doFindEntry(fileExtension);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private Collection<ArchiveEntry> doFindEntry(String fileExtension) throws IOException {
        ImmutableSet.Builder<ArchiveEntry> result = ImmutableSet.builder();
        for (var entry = stream.getNextEntry();
             entry != null;
             entry = stream.getNextEntry()) {
            var entryName = entry.getName();
            if (entryName.endsWith(fileExtension)) {
                logger.atDebug().log(() -> format("Reading ZIP entry `%s`.", entryName));
                var read = readEntry();
                result.add(read);
            }
        }
        return result.build();
    }

    private ArchiveEntry readEntry() throws IOException {
        // When being read, a `ZipStream` reports to be out of data when the *current entry* is
        // over, not when the whole archive is read.
        var bytes = toByteArray(stream);
        return ArchiveEntry.of(bytes);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the underlying file stream.
     *
     * @throws IOException
     *         if {@code InputStream.close()} throws an {@code IOException}
     */
    @Override
    public void close() throws IOException {
        stream.close();
    }
}
