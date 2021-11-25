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

import com.google.common.annotations.VisibleForTesting;
import io.spine.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;

/**
 * Creates {@link #BUILD_GRADLE build.gradle} OR {@link #BUILD_GRADLE_KTS build.gradle.kts} file in
 * the directory of the project, copying it from resources.
 *
 * <p>{@code build.gradle} has a priority over {@code build.gradle.kts}. If both files are
 * present in resources, only {@code build.gradle} file will be copied.
 *
 * <p>If none of the files are available from resources {@code IllegalStateException}
 * will be thrown.
 */
final class BuildGradle {

    /** The name of the Gradle build file. */
    @VisibleForTesting
    static final String BUILD_GRADLE = "build.gradle";

    /** The name of the Gradle Kotlin Script build file. */
    private static final String BUILD_GRADLE_KTS = "build.gradle.kts";

    private final Path projectDir;

    BuildGradle(Path projectDir) {
        this.projectDir = checkNotNull(projectDir);
    }

    /**
     * Copies a Gradle build script from the classpath into the test project directory.
     *
     * @throws IOException
     *         if the file cannot be written
     * @throws IllegalStateException
     *         if none of the build scripts were found in resources
     */
    void createFile() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        Resource buildGradle = Resource.file(BUILD_GRADLE, classLoader);
        Resource buildGradleKts = Resource.file(BUILD_GRADLE_KTS, classLoader);
        Path targetPath;
        Resource file;
        if (buildGradle.exists()) {
            targetPath = projectDir.resolve(BUILD_GRADLE);
            file = buildGradle;
        } else if (buildGradleKts.exists()) {
            targetPath = projectDir.resolve(BUILD_GRADLE_KTS);
            file = buildGradleKts;
        } else {
            throw newIllegalStateException(
                    "Neither `%s` nor `%s` were found in resources.",
                    BUILD_GRADLE, BUILD_GRADLE_KTS
            );
        }

        try (InputStream fileContent = file.open()) {
            createDirectories(targetPath.getParent());
            checkNotNull(fileContent);
            copy(fileContent, targetPath);
        }
    }
}
