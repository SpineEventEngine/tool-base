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

package io.spine.tools.type;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.spine.annotation.Internal;
import io.spine.code.proto.FileDescriptorSetReader;
import io.spine.logging.WithLogging;
import io.spine.tools.archive.ArchiveEntry;
import io.spine.tools.archive.ArchiveFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.newHashSet;
import static io.spine.code.proto.FileDescriptors.DESC_EXTENSION;
import static io.spine.tools.archive.ArchiveFile.isArchive;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

/**
 * A set of {@code FileDescriptorSet}s.
 */
@Internal
public final class FileDescriptorSuperset implements WithLogging {

    private final Set<FileDescriptorSet> descriptors;

    /**
     * Creates a new instance.
     */
    public FileDescriptorSuperset() {
        this.descriptors = newHashSet();
    }

    /**
     * Flattens this superset into a single descriptor set.
     *
     * <p>The descriptors in the output set are de-duplicated and unordered.
     *
     * @return the result of the sets merging
     */
    public MergedDescriptorSet merge() {
        var allFiles = descriptors.stream()
                .flatMap(set -> set.getFileList().stream())
                .collect(toSet());
        var descriptorSet = FileDescriptorSet.newBuilder()
                .addAllFile(allFiles)
                .build();
        return new MergedDescriptorSet(descriptorSet);
    }

    public void addFromDependency(File dependencyFile) {
        checkNotNull(dependencyFile);
        logger().atDebug().log(() -> format("Loading descriptors from `%s`.", dependencyFile));
        readDependency(dependencyFile)
                .forEach(this::addFiles);
    }

    private void addFiles(FileDescriptorSet fileSet) {
        descriptors.add(fileSet);
    }

    private Collection<FileDescriptorSet> readDependency(File file) {
        if (file.isDirectory()) {
            return mergeDirectory(file);
        } else if (isArchive(file)) {
            return readFromArchive(file);
        } else {
            return readFromPlainFile(file)
                    .map(ImmutableSet::of)
                    .orElse(ImmutableSet.of());
        }
    }

    private ImmutableSet<FileDescriptorSet> mergeDirectory(File directory) {
        var descriptorFiles = directory.listFiles(
                (dir, name) -> name.endsWith(DESC_EXTENSION)
        );
        checkState(descriptorFiles != null,
                   "Unable to load descriptor files from the directory: `%s`.", directory);
        if (descriptorFiles.length == 0) {
            logger().atDebug().log(() -> format(
                "No descriptors found in the directory: `%s`.", directory
            ));
            return ImmutableSet.of();
        } else {
            var result =
                    Stream.of(descriptorFiles)
                          .map(this::read)
                          .collect(toImmutableSet());
            return result;
        }
    }

    private ImmutableSet<FileDescriptorSet> readFromArchive(File archiveFile) {
        var archive = ArchiveFile.from(archiveFile);
        var result =
                archive.findByExtension(DESC_EXTENSION)
                       .stream()
                       .map(ArchiveEntry::asDescriptorSet)
                       .collect(toImmutableSet());
        if (!result.isEmpty()) {
            logger().atDebug().log(() -> format(
                "Found %d descriptor set file(s) in archive `%s`.", result.size(), archiveFile
            ));
        }
        return result;
    }

    private FileDescriptorSet read(File file) {
        checkArgument(file.exists(), "File does not exist: `%s`.", file);
        var path = file.toPath();
        logger().atDebug().log(() -> format("Reading descriptors from file `%s`.", file));
        try {
            var bytes = Files.readAllBytes(path);
            return FileDescriptorSetReader.parse(bytes);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private Optional<FileDescriptorSet> readFromPlainFile(File file) {
        if (file.getName().endsWith(DESC_EXTENSION)) {
            var result = read(file);
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }
}
