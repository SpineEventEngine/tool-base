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

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.Arrays;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection of {@link JavaCompile} tasks in a project.
 */
public final class JavaCompileTasks implements Iterable<JavaCompile> {

    private final TaskCollection<JavaCompile> tasks;

    private JavaCompileTasks(Project project) {
        var allTasks = project.getTasks();
        this.tasks = allTasks.withType(JavaCompile.class);
    }

    /**
     * Creates a new instance for the given project.
     */
    public static JavaCompileTasks of(Project project) {
        checkNotNull(project);
        return new JavaCompileTasks(project);
    }

    /**
     * Adds specified arguments to all {@code JavaCompile} tasks of the project.
     */
    public void addArgs(String... arguments) {
        checkNotNull(arguments);
        for (var task : tasks) {
            var taskOptions = task.getOptions();
            var compilerArgs = taskOptions.getCompilerArgs();
            compilerArgs.addAll(Arrays.asList(arguments));
        }
    }

    @Override
    public Iterator<JavaCompile> iterator() {
        return tasks.iterator();
    }
}
