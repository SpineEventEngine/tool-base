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

import com.google.common.collect.Multimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.gradle.SourceSetName;
import io.spine.tools.gradle.task.TaskName;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;

/**
 * Allows to configure a Gradle project for testing needs.
 *
 * <p>The project operates in the given test project directory and allows executing Gradle tasks.
 */
public final class GradleProject {

    /** The name of the directory under {@code resources} which contains files for this project. */
    private final String origin;

    private final GradleRunner runner;
    private final RunnerArguments arguments;

    /**
     * Creates new builder for the project.
     */
    public static GradleProjectSetup fromResources() {
        return new GradleProjectSetup();
    }

    /**
     * Obtains the name of the Java Gradle plugin.
     */
    public static String javaPlugin() {
        return "java";
    }

    GradleProject(GradleProjectSetup builder) throws IOException {
        this.origin = builder.origin();
        this.arguments = builder.arguments();
        this.runner = GradleRunner.create()
                .withProjectDir(builder.dir())
                .withDebug(builder.debug());
        if (builder.addPluginUnderTestClasspath()) {
            runner.withPluginClasspath();
        }
        if (builder.environment() != null) {
            runner.withEnvironment(builder.environment());
        }
        writeGradleScripts();
        writeBuildSrc();
        writeProtoFiles(builder.protoFileNames());
        writeJavaFiles(builder.javaFileNames());
    }

    /**
     * Expose {@link GradleRunner} used by this Gradle project for finer tuning.
     */
    @SuppressWarnings("unused")
    public GradleRunner runner() {
        return runner;
    }

    private void writeGradleScripts() throws IOException {
        Path projectDir = projectDir();
        BuildGradle buildGradle = new BuildGradle(projectDir);
        buildGradle.createFile();

        TestEnvGradle testEnvGradle = new TestEnvGradle(projectDir);
        testEnvGradle.createFile();
    }

    /** The directory of this project. */
    private Path projectDir() {
        return runner.getProjectDir()
                     .toPath();
    }

    private void writeBuildSrc() throws IOException {
        Path targetDir = projectDir();
        BuildSrc.writeTo(targetDir);
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

    @CanIgnoreReturnValue
    public BuildResult executeTask(TaskName taskName) {
        return prepareRun(taskName).build();
    }

    @CanIgnoreReturnValue
    public BuildResult executeAndFail(TaskName taskName) {
        return prepareRun(taskName).buildAndFail();
    }

    private GradleRunner prepareRun(TaskName taskName) {
        String[] args = arguments.forTask(taskName);
        return runner.withArguments(args);
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
        String resourcePath = origin + '/' + filePath;
        try (InputStream fileContent = openResource(resourcePath)) {
            Path fileSystemPath = projectDir().resolve(filePath);
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
}
