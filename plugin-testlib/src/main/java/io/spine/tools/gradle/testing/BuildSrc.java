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

import io.spine.tools.fs.DirectoryName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;

import static io.spine.io.Copy.copyDir;

/**
 * Utilities for working with the {@code buildScr} directory of a Gradle project.
 */
final class BuildSrc {

    /** Prevents instantiation of this utility class. */
    private BuildSrc() {
    }

    /**
     * Copies the {@code buildSrc} directory from the {@link RootProject} into
     * the specified directory.
     */
    static void writeTo(Path targetDir) throws IOException {
        Path projectRoot = RootProject.path();
        Path buildSrc = projectRoot.resolve("buildSrc");
        copyDir(buildSrc, targetDir, new SkipNonSrcDirs());
    }

    /**
     * The predicate to prevent copying unnecessary files when copying
     * the {@code buildSrc} directory from the parent project.
     *
     * <p>The predicate 1) saves on unnecessary copying, 2) prevents file locking issue
     * under Windows, which fails the build because locked under the {@code .gradle}
     * directory could not be copied.
     */
    private static class SkipNonSrcDirs implements Predicate<Path> {

        @Override
        public boolean test(Path path) {
            String str = path.toString();
            String slash = File.separator;
            // Use leading slash to accept `.gradle` files, but filter out the Gradle cache dir.
            boolean isGradleCache = str.contains(slash + ".gradle");

            // Use two slashes to accept `build.gradle.kts`, but filter out the `build` dir.
            boolean isBuildDir = str.contains(slash + DirectoryName.build.value() + slash);
            return !isGradleCache && !isBuildDir;
        }
    }
}
