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
import com.google.common.collect.Multimap;
import io.spine.tools.gradle.SourceSetName;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.lang.String.format;
import static java.nio.file.Files.copy;
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
        copyBuildSrc();
        writeGradleScripts();
        writeProtoFiles(setup.protoFileNames());
        writeJavaFiles(setup.javaFileNames());
        createFiles(setup.filesToCreate());
    }

    private String origin() {
        return setup.origin();
    }

    private Path projectDir() {
        return setup.projectDir().toPath();
    }

    private Path resolve(String path) {
        Path sourcePath = projectDir().resolve(path);
        return sourcePath;
    }

    private void writeGradleScripts() throws IOException {
        Path projectDir = projectDir();
        BuildGradle buildGradle = new BuildGradle(projectDir);
        buildGradle.createFile();

        TestEnvGradle testEnvGradle = new TestEnvGradle(projectDir);
        testEnvGradle.createFile();
    }

    private void copyBuildSrc() throws IOException {
        BuildSrc.writeTo(projectDir());
    }

    private void writeProtoFiles(Multimap<SourceSetName, String> fileNames) throws IOException {
        for (Map.Entry<SourceSetName, String> protoFile : fileNames.entries()) {
            writeProto(protoFile.getKey(), protoFile.getValue());
        }
    }

    private void writeJavaFiles(Multimap<SourceSetName, String> fileNames) throws IOException {
        for (Map.Entry<SourceSetName, String> javaFile : fileNames.entries()) {
            writeJava(javaFile.getKey(), javaFile.getValue());
        }
    }

    private void writeProto(SourceSetName ssn, String fileName) throws IOException {
        writeFile(fileName, protoDir(ssn));
    }

    static String protoDir(SourceSetName ssn) {
        return format("src/%s/proto/", ssn);
    }

    private void writeJava(SourceSetName ssn, String fileName) throws IOException {
        writeFile(fileName, javaDir(ssn));
    }

    private static String javaDir(SourceSetName ssn) {
        return format("src/%s/java/", ssn);
    }

    private void writeFile(String fileName, String dir) throws IOException {
        String filePath = dir + fileName;
        String resourcePath = origin() + '/' + filePath;
        try (InputStream fileContent = openResource(resourcePath)) {
            Path fileSystemPath = resolve(filePath);
            createDirectories(fileSystemPath.getParent());
            copy(fileContent, fileSystemPath);
        }
    }

    private InputStream openResource(String fullPath) {
        InputStream stream = getClass().getClassLoader()
                                       .getResourceAsStream(fullPath);
        checkState(stream != null, "Unable to locate resource: `%s`.", fullPath);
        return stream;
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
}
