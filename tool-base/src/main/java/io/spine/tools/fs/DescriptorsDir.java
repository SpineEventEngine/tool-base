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

package io.spine.tools.fs;

import com.google.errorprone.annotations.Immutable;
import io.spine.code.fs.AbstractDirectory;

import java.nio.file.Path;

import static io.spine.tools.fs.DirectoryName.descriptors;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A directory with descriptor files.
 */
@Immutable
public final class DescriptorsDir extends AbstractDirectory {

    private DescriptorsDir(BuildRoot parent, String name) {
        super(parent.path().resolve(name));
    }

    DescriptorsDir(BuildRoot parent) {
        this(parent, descriptors.value());
    }

    /**
     * Obtains the path to a sub-directory containing descriptor set files
     * for the source set of interest.
     *
     * @param sourceSet
     *          the name of the source set, such as {@code "main"} or {@code "test"}
     */
    public Path forSourceSet(String sourceSet) {
        checkNotEmptyOrBlank(sourceSet);
        return path().resolve(sourceSet);
    }
}
