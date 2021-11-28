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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import io.spine.io.ResourceDirectory;
import io.spine.tools.gradle.SourceSetName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;

/**
 * Handles writing all source code for a new {@link GradleProject}.
 */
final class Sources {

    private final GradleProjectSetup setup;

    Sources(GradleProjectSetup setup) {
        this.setup = checkNotNull(setup);
    }

    /**
     * Creates source code files for a new {@link GradleProject}.
     */
    void write() throws IOException {
        if (setup.needsBuildSrc()) {
            BuildSrc.writeTo(projectDir());
        }
        createTestEnv();
        copyFromResources();
        createFiles(setup.filesToCreate());
    }

    private Path projectDir() {
        return setup.projectDir().toPath();
    }

    private void createTestEnv() throws IOException {
        Path projectDir = projectDir();
        TestEnvGradle testEnvGradle = new TestEnvGradle(projectDir);
        testEnvGradle.createFile();
    }

    static String protoDir(SourceSetName ssn) {
        return format("src/%s/proto/", ssn);
    }

    private void copyFromResources() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        ResourceDirectory directory = ResourceDirectory.get(origin(), classLoader);
        directory.copyContentTo(projectDir());
    }

    private String origin() {
        String resourceDir = setup.resourceDir();
        checkState(resourceDir != null,
                   "The project is not configured to load files from resources." +
                           " Please call `%s.setOrigin(String)`.",
                   GradleProjectSetup.class.getSimpleName());
        return resourceDir;
    }

    private void createFiles(Map<String, ImmutableList<String>> files) {
        files.forEach(this::createFile);
    }

    private void createFile(String path, Iterable<String> lines) {
        Path sourcePath = resolve(path);
        try {
            createDirectories(sourcePath.getParent());
            Files.write(sourcePath, lines, Charsets.UTF_8);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private Path resolve(String path) {
        Path sourcePath = projectDir().resolve(path);
        return sourcePath;
    }
}
