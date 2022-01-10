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

package io.spine.tools.js.fs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.fs.AbstractFileName;
import io.spine.tools.fs.FileReference;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.repeat;
import static io.spine.tools.fs.FileReference.currentDirectory;
import static io.spine.tools.fs.FileReference.parentDirectory;
import static io.spine.tools.fs.FileReference.splitter;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A name of a JavaScript file, which is generated under the the root directory with compiled
 * Protobuf files.
 */
@Immutable
public final class FileName extends AbstractFileName<FileName> {

    private static final long serialVersionUID = 0L;

    /** The suffix automatically appended to the files generated by Protobuf JS. */
    private static final String SUFFIX = "_pb";

    /** The standard file extension. */
    private static final String EXTENSION = ".js";

    /** The ending of a {@code .proto} file compiled into JavaScript. */
    private static final String PB_SUFFIX = SUFFIX + EXTENSION;

    private FileName(String value) {
        super(value);
    }

    /**
     * Creates new JavaScript file name with the passed value.
     */
    static FileName of(String value) {
        checkNotEmptyOrBlank(value);
        checkArgument(value.endsWith(EXTENSION),
                      "`%s` is expected to contain `.js` extension.", value);
        return new FileName(value);
    }

    /**
     * Obtains the name of the generated JavaScript file using the file descriptor.
     *
     * <p>All generated files have {@code _pb.js} suffix,
     * so the descriptor with name {@code spine/options.proto}
     * becomes {@code spine/options_pb.js}.
     *
     * <p>The resulting name also includes the path denoting a Protobuf package.
     *
     * @param descriptor
     *         the descriptor of the file
     * @return the name of a generated JavaScript file
     */
    public static FileName from(FileDescriptor descriptor) {
        checkNotNull(descriptor);
        var protoFileName = io.spine.code.proto.FileName.from(descriptor);
        var fileName = protoFileName.nameWithoutExtension() + PB_SUFFIX;
        return of(fileName);
    }

    /**
     * Composes the path from the given file to its root.
     *
     * <p>Basically, the method replaces all preceding path elements
     * by the {@link FileReference#parentDirectory()}.
     */
    public String pathToRoot() {
        var pathElements = pathElements();
        var fileLocationDepth = pathElements.size() - 1;
        var pathToRoot = repeat(parentDirectory(), fileLocationDepth);
        var result = pathToRoot.isEmpty()
                     ? currentDirectory()
                     : pathToRoot;
        return result;
    }

    /**
     * Obtains the path to the file.
     *
     * <p>Assumes that the path is used at the root directory with generated Protobuf files.
     */
    public String pathFromRoot() {
        return currentDirectory() + value();
    }

    /**
     * Returns all {@code FileName} elements, i.e. the relative path to the file and file name
     * itself.
     */
    @VisibleForTesting
    List<String> pathElements() {
        var elements = splitter().split(value());
        return ImmutableList.copyOf(elements);
    }
}
