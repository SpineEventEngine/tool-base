/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import io.spine.code.fs.SourceCodeDirectory;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A folder with JavaScript source files.
 *
 * @deprecated please use {@link JsFiles}.
 */
@Deprecated
public final class Directory extends SourceCodeDirectory {

    private Directory(Path path) {
        super(path);
    }

    /**
     * Creates a new instance at the specified location.
     */
    public static Directory at(Path path) {
        checkNotNull(path);
        return new Directory(path);
    }

    /**
     * Obtains the source code path for the passed file name.
     */
    public Path resolve(FileName fileName) {
        return JsFiles.resolve(this, fileName);
    }

    /**
     * Obtains the source code path for the passed library file.
     */
    public Path resolve(LibraryFile libraryFile) {
        checkNotNull(libraryFile);
        return JsFiles.resolve(this, libraryFile.fileName());
    }
}
