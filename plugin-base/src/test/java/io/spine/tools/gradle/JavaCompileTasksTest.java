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

package io.spine.tools.gradle;

import io.spine.tools.gradle.given.StubProject;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.gradle.given.ProjectConfigurations.assertCompileTasksContain;
import static io.spine.tools.gradle.given.ProjectConfigurations.assertCompileTasksEmpty;

@DisplayName("`JavaCompileTasks` should")
class JavaCompileTasksTest {

    private Project project;

    @BeforeEach
    void createProject() {
        project = StubProject.createFor(getClass()).get();
    }

    @Test
    @DisplayName("add arguments to Java compile tasks")
    void someArgs() {
        String firstArg = "firstArg";
        String secondArg = "secondArg";
        JavaCompileTasks.of(project).addArgs(firstArg, secondArg);
        assertCompileTasksContain(project, firstArg, secondArg);
    }

    @Test
    @DisplayName("not add arguments if none is specified")
    void noArgs() {
        JavaCompileTasks.of(project).addArgs();
        assertCompileTasksEmpty(project);
    }
}