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

package io.spine.tools.java.fs;

import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.Internal;
import io.spine.code.fs.AbstractDirectory;
import io.spine.tools.code.SourceSetName;
import io.spine.tools.fs.DefaultPaths;
import io.spine.tools.fs.DirectoryName;
import io.spine.tools.fs.Generated;
import io.spine.tools.fs.SourceDir;
import io.spine.tools.fs.Src;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A default directory structure for a Spine-based Java project.
 *
 * <p>The project structure is based on the standard Maven/Gradle project conventions, with the
 * following directories under the project root:
 *
 * <ul>
 * <li>{@code src/main} — manually written code production code, with {@code java} and
 * {@code proto} sub-directories.
 *
 * <li>{@code src/test} — the code of tests.
 *
 * <li>{@code generated} — computer-generated code copied from
 *   {@code build/generated/source/proto} and processed by ProtoData.
 * </ul>
 */
@Internal
@Immutable
public final class DefaultJavaPaths extends DefaultPaths {

    private static final String ROOT_NAME = DirectoryName.java.value();

    private DefaultJavaPaths(Path projectDir) {
        super(projectDir);
    }

    public static DefaultJavaPaths at(Path projectDir) {
        checkNotNull(projectDir);
        var result = new DefaultJavaPaths(projectDir);
        return result;
    }

    public static DefaultJavaPaths at(File projectDir) {
        checkNotNull(projectDir);
        return at(projectDir.toPath());
    }

    @SuppressWarnings("unused") /* Part of the public API. */
    public Src src() {
        return new Src(projectDir());
    }

    @Override
    public GeneratedJava generated() {
        return new GeneratedJava(projectDir());
    }

    /**
     * The directory with the generated Java code processed by ProtoData.
     */
    public static final class GeneratedJava extends Generated {

        private GeneratedJava(AbstractDirectory projectDir) {
            super(projectDir);
        }

        @Override
        public SourceDir dir(SourceSetName ssn) {
            return subDir(ssn, ROOT_NAME);
        }
    }
}
