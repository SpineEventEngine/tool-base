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

package io.spine.tools.gradle.testing;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.Files.exists;
import static java.util.Objects.requireNonNull;

/**
 * Utilities for obtaining properties of a project which runs {@link GradleProject} in its tests.
 */
final class RootProject {

    private static final String VERSION_GRADLE = "version.gradle";
    private static final String VERSION_GRADLE_KTS = "version.gradle.kts";

    /** Prevents instantiation of this utility class. */
    private RootProject() {
    }

    /**
     * Finds a root of a project by presence of the {@link #VERSION_GRADLE version.gradle} or
     * {@link #VERSION_GRADLE_KTS version.gradle.kts} file.
     *
     * <p>Starts from the current directory, climbing up, until the file is found. By convention
     * a project should have only one version file, which is placed in the root directory of
     * the project.
     *
     * @throws IllegalStateException
     *         if the {@link #VERSION_GRADLE version.gradle.kts} file is not found
     */
    static Path path() {
        Path workingFolderPath = Paths.get(".")
                                      .toAbsolutePath();
        @Nullable Path extGradleDirPath = workingFolderPath;
        while (extGradleDirPath != null && !hasVersionGradle(extGradleDirPath)) {
            extGradleDirPath = extGradleDirPath.getParent();
        }
        checkState(extGradleDirPath != null,
                   "Neither `%s` nor `%s` found in `%s` or parent directories.",
                   VERSION_GRADLE,
                   VERSION_GRADLE_KTS,
                   workingFolderPath);
        return requireNonNull(extGradleDirPath);
    }

    private static boolean hasVersionGradle(Path directory) {
        return exists(directory.resolve(VERSION_GRADLE)) ||
               exists(directory.resolve(VERSION_GRADLE_KTS));
    }

    /**
     * Same as {@link #path()}, but returning {@code File} instance.
     *
     * @throws IllegalStateException
     *         if the {@link #VERSION_GRADLE version.gradle.kts} file is not found
     * @see #path()
     */
    static File dir() {
        return path().toFile();
    }
}
