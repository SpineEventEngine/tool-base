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
package io.spine.tools.gradle.task;

import com.google.common.annotations.VisibleForTesting;

/**
 * Names of Gradle tasks defined by the {@code base} plugin.
 *
 * @see <a href="https://docs.gradle.org/current/userguide/base_plugin.html#sec:base_tasks">
 *         the plugin doc</a>
 */
@SuppressWarnings("unused")
public enum BaseTaskName implements TaskName {

    /**
     * Deletes the temporary build artifacts.
     */
    clean,

    /**
     * The aggregate task that assembles all the artifacts of this project.
     */
    assemble,

    /**
     * A lifecycle task which marks the project verification routines, such as static code analysis,
     * executing tests, etc.
     */
    check,

    /**
     * A lifecycle task which builds everything in the project, including running tests, producing
     * production artifacts, and generating documentation.
     */
    build,

    /**
     * The task that prints basic Gradle usage instructions.
     *
     * <p>This task is primarily used in tests on bare-bones Gradle projects where other
     * tasks are not available.
     */
    @VisibleForTesting
    help
}
