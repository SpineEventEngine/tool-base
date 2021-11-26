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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.gradle.task.TaskName;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Allows to configure a Gradle project for testing needs.
 *
 * <p>The project operates in the given test project directory and allows executing Gradle tasks.
 */
public final class GradleProject {

    private final GradleRunner runner;
    private final RunnerArguments arguments;

    /**
     * Starts creation of a new the project.
     *
     * @param projectDir
     *         the name of the directory on the file system under which the project
     *         will be created
     */
    public static GradleProjectSetup setupAt(File projectDir) {
        checkNotNull(projectDir);
        return new GradleProjectSetup(projectDir);
    }

    /**
     * Obtains the name of the Java Gradle plugin.
     */
    public static String javaPlugin() {
        return "java";
    }

    GradleProject(GradleProjectSetup setup) throws IOException {
        this.arguments = setup.arguments();
        this.runner = GradleRunner.create()
                .withProjectDir(setup.projectDir())
                .withDebug(setup.debug());
        if (setup.addPluginUnderTestClasspath()) {
            runner.withPluginClasspath();
        }
        if (setup.environment() != null) {
            runner.withEnvironment(setup.environment());
        }
        writeSources(setup);
    }

    private static void writeSources(GradleProjectSetup setup) throws IOException {
        Sources sources = new Sources(setup);
        sources.write();
    }

    /**
     * Expose {@link GradleRunner} used by this Gradle project for finer tuning.
     */
    @SuppressWarnings("unused")
    public GradleRunner runner() {
        return runner;
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
}
