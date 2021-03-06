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

import com.google.common.collect.ImmutableList;
import io.spine.code.fs.AbstractSourceFile;

import java.nio.file.Path;

/**
 * A source code file containing import statements that may need to
 * be {@linkplain #resolveImports(Path, ExternalModules) resolved}.
 */
public abstract class FileWithImports extends AbstractSourceFile {

    protected FileWithImports(Path path) {
        super(path);
    }

    /**
     * Resolves the relative imports in the file into absolute ones with the given modules.
     */
    @SuppressWarnings("unused") /* Part of the public API. */
    public void resolveImports(Path generatedRoot, ExternalModules modules) {
        load();
        ImmutableList.Builder<String> newLines = ImmutableList.builder();
        for (var line : lines()) {
            if (isImport(line)) {
                var resolved = resolveImport(line, generatedRoot, modules);
                newLines.add(resolved);
            } else {
                newLines.add(line);
            }
        }
        update(newLines.build());
        store();
    }

    /**
     * Tests if the passed line contains an import statement.
     */
    protected abstract boolean isImport(String line);

    /**
     * Transforms the import statement of the passed line updating the imported file
     * reference in relation to the passed root directory and external modules.
     */
    protected abstract
    String resolveImport(String line, Path generatedRoot, ExternalModules modules);
}
