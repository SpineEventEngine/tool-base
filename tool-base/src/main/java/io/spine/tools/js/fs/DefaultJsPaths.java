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

import com.google.errorprone.annotations.Immutable;
import io.spine.code.fs.AbstractDirectory;
import io.spine.tools.code.SourceSetName;
import io.spine.tools.fs.DefaultPaths;
import io.spine.tools.fs.Generated;
import io.spine.tools.fs.SourceDir;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A default directory structure for a Spine-based JavaScript project.
 *
 * <p>The project structure reflects the conventions currently used in Spine, and contains at least
 * the following directories:
 *
 * <ul>
 * <li>{@code build}
 * <ul>
 *     <li>{@code descriptors}
 *     <ul>
 *         <li>{@code main}
 *         <ul>
 *             <li>{@code known_types.desc} — descriptors for "main" source set.
 *         </ul>
 *         <li>{@code test}
 *         <ul>
 *             <li>{@code known_types.desc} — descriptors for "test" source set.
 *         </ul>
 *     </ul>
 * </ul>
 *
 * <li>{@code generated} — the JS code, generated by Protobuf JS compiler.
 * <ul>
 *     <li>{@code main}
 *     <ul>
 *         <li>{@code js} — generated messages from "main" source set.
 *     </ul>
 *     <li>{@code test}
 *     <ul>
 *         <li>{@code js} — generated messages from "test" source set.
 *     </ul>
 * </ul>
 * </li>
 * </ul>
 *
 * <p>Other directories (like source code directory) may also be present in the project, but their
 * location is currently not standardized and thus is not described by this class.
 */
@Immutable
@SuppressWarnings("unused") /* Part of the public API. */
public final class DefaultJsPaths extends DefaultPaths {

    private static final String ROOT_NAME = "js";

    private DefaultJsPaths(Path projectDir) {
        super(projectDir);
    }

    public static DefaultJsPaths at(Path projectDir) {
        checkNotNull(projectDir);
        var result = new DefaultJsPaths(projectDir);
        return result;
    }

    public static DefaultJsPaths at(File projectDir) {
        checkNotNull(projectDir);
        return at(projectDir.toPath());
    }

    @Override
    public GeneratedJs generated() {
        return new GeneratedJs(projectDir());
    }

    /**
     * The directory with the generated JavaScript code.
     */
    public static final class GeneratedJs extends Generated {

        private GeneratedJs(AbstractDirectory projectDir) {
            super(projectDir);
        }

        @Override
        public SourceDir dir(SourceSetName ssn) {
            return subDir(ssn, ROOT_NAME);
        }
    }
}
