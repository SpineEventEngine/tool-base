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

package io.spine.tools.gradle.task;

import com.google.common.collect.ImmutableList;
import io.spine.tools.gradle.testing.NoOp;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskInputs;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.tools.gradle.task.BaseTaskName.clean;
import static io.spine.tools.gradle.task.GivenTaskName.annotateProto;
import static io.spine.tools.gradle.task.GivenTaskName.preClean;
import static io.spine.tools.gradle.task.GivenTaskName.verifyModel;
import static io.spine.tools.gradle.task.JavaTaskName.classes;
import static io.spine.tools.gradle.task.JavaTaskName.compileJava;
import static io.spine.tools.gradle.task.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.task.ProtobufTaskName.generateTestProto;
import static io.spine.tools.gradle.testing.GradleProject.javaPlugin;
import static io.spine.tools.gradle.testing.NoOp.action;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests {@link GradleTask.Builder}.
 */
@DisplayName("`GradleTask.Builder` should")
class GradleTaskBuilderTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                                .build();
        project.getPluginManager()
               .apply(javaPlugin);
    }

    @Test
    @DisplayName("create task dependant on all tasks of given name")
    void createTaskDependantOnAllTasksOfGivenName() {
        var subProject = ProjectBuilder.builder()
                .withParent(project)
                .build();
        subProject.getPluginManager()
                  .apply(javaPlugin);
        var task = GradleTask.newBuilder(annotateProto, NoOp.action())
                .insertAfterAllTasks(compileJava)
                .applyNowTo(subProject);
        var subProjectTasks = subProject.getTasks();
        var newTask = subProjectTasks.findByName(task.getName().name());
        assertThat(newTask)
                .isNotNull();
        Collection<?> dependencies = newTask.getDependsOn();

        assertThat(dependencies)
                .containsAtLeast(
                        subProjectTasks.findByName(compileJava.name()),
                        project.getTasks()
                               .findByName(compileJava.name())
                );
    }

    @Test
    @DisplayName("create task and insert before other")
    void createTaskAndInsertBeforeOther() {
        GradleTask.newBuilder(verifyModel, NoOp.action())
              .insertBeforeTask(classes)
              .applyNowTo(project);
        var tasks = project.getTasks();

        var classes = tasks.findByName(JavaTaskName.classes.name());
        assertThat(classes)
                .isNotNull();

        var verifyModelTask = tasks.findByName(verifyModel.name());
        assertThat(classes.getDependsOn())
                .contains(verifyModelTask);
    }

    @Test
    @DisplayName("create task and insert after other")
    void createTaskAndInsertAfterOther() {
        GradleTask.newBuilder(verifyModel, NoOp.action())
              .insertAfterTask(compileJava)
              .applyNowTo(project);
        var tasks = project.getTasks();

        var compileJavaTask = tasks.findByName(compileJava.name());
        assertThat(compileJavaTask)
                .isNotNull();
        var verifyModelTask = tasks.findByName(verifyModel.name());

        assertThat(verifyModelTask)
                .isNotNull();
        assertThat(verifyModelTask.getDependsOn())
                .contains(compileJavaTask.getName());
    }

    @Test
    @DisplayName("ignore task dependency if no such task found")
    void ignoreTaskDependencyIfNoSuchTaskFound() {
        GradleTask.newBuilder(generateTestProto, NoOp.action())
              .insertAfterAllTasks(generateProto)
              .applyNowTo(project);
        var tasks = project.getTasks();

        var generateProtoTask = tasks.findByName(generateProto.name());
        assertThat(generateProtoTask)
                .isNull();

        var generateTestProtoTask = tasks.findByName(generateTestProto.name());
        assertThat(generateTestProtoTask)
                .isNotNull();
    }

    @Test
    @DisplayName("not allow tasks without any connection to task graph")
    void notAllowTasksWithoutAnyConnectionToTaskGraph() {
        var builder = GradleTask.newBuilder(verifyModel, action());
        assertIllegalState(() -> builder.applyNowTo(project));
    }

    @Test
    @DisplayName("return build task description")
    void returnBuildTaskDescription() {
        var desc = GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(clean)
                .applyNowTo(project);
        assertThat(desc.getName())
                .isEqualTo(preClean);
        assertThat(desc.getProject())
                .isEqualTo(project);
    }

    @Test
    @DisplayName("create task with given inputs")
    void createTaskWithGivenInputs() throws IOException {
        var input = new File(".").getAbsoluteFile();
        var files = project.getLayout().files(input);
        GradleTask.newBuilder(preClean, NoOp.action())
              .insertBeforeTask(clean)
              .withInputFiles(files)
              .applyNowTo(project);
        var task = project.getTasks()
                          .findByPath(preClean.name());
        assertNotNull(task);
        @Nullable TaskInputs inputs = task.getInputs();
        assertNotNull(inputs);

        var inputFiles = ImmutableList.copyOf(inputs.getFiles().getFiles());
        assertThat(inputFiles)
                .hasSize(1);
        assertThat(inputFiles.get(0).getCanonicalFile())
                .isEqualTo(input.getCanonicalFile());
    }
}
