/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.gradle.project.ProjectHierarchy;
import org.jspecify.annotations.Nullable;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Utility wrapper around the Gradle tasks created.
 *
 * <p>Instantiated via {@link Builder}, forces the new task to be added to
 * the Gradle build lifecycle.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") /* Instantiated via Builder. */
public final class GradleTask {

    private final Task task;
    private final TaskName name;
    private final Project project;

    private GradleTask(Task task, TaskName name, Project project) {
        this.task = task;
        this.name = name;
        this.project = project;
    }

    /**
     * Creates a builder for a new task.
     *
     * @param name
     *         the name of the task
     * @param action
     *         the configuration action for the task
     */
    public static Builder newBuilder(TaskName name, Action<Task> action) {
        checkNotNull(name);
        checkNotNull(action);
        return new Builder(name, action);
    }

    /**
     * Creates a new instance from the specified {@code Task}.
     */
    public static GradleTask from(Task task) {
        checkNotNull(task);
        TaskName taskName = new DynamicTaskName(task.getName());
        var project = task.getProject();
        return new GradleTask(task, taskName, project);
    }

    /**
     * Obtains the Gradle task itself.
     */
    public Task getTask() {
        return task;
    }

    /**
     * Obtains the task name.
     */
    public TaskName getName() {
        return name;
    }

    /**
     * Obtains the project of the task.
     */
    public Project getProject() {
        return project;
    }

    /**
     * A builder for {@link GradleTask}.
     *
     * <p>NOTE: unlike most classes following the {@code Builder} pattern,
     * this one provides {@link #applyNowTo(Project)} method instead of
     * {@code build(..)}. This is done to add some additional semantics to
     * such an irreversible action like this.
     */
    @SuppressWarnings("unused")
    public static final class Builder {
        private final TaskName name;
        private final Action<Task> action;

        private TaskName previousTask;
        private TaskName previousTaskOfAllProjects;
        private TaskName followingTask;

        private boolean allowNoDependencies;

        private boolean hasInputFiles = false;
        private final Set<File> inputs = new HashSet<>();
        private Map<String, @Nullable Object> inputProperties;
        private boolean hasOutputFiles = false;
        private final Set<File> outputs = new HashSet<>();

        private Builder(TaskName name, Action<Task> action) {
            this.name = name;
            this.action = action;
        }

        /**
         * Specify a task which will follow the new one.
         *
         * <p> Once built, the new instance of {@link GradleTask} will be inserted
         * before the anchor.
         *
         * <p> NOTE: invocation of either this method or {@link #insertAfterTask} is mandatory,
         * as the newly created instance of {@link GradleTask} must be put to
         * a certain place in the Gradle build lifecycle.
         *
         * @param target
         *         the name of the task, serving as "before" anchor
         * @return the current instance of {@code Builder}
         */
        public Builder insertBeforeTask(TaskName target) {
            checkNotNull(target, "Task after the new one");
            checkState(dependenciesRequired());
            this.followingTask = target;
            return this;
        }

        /**
         * Specify a task which will precede the new one.
         *
         * <p> Once built, the new instance of {@link GradleTask} will be inserted
         * after the anchor.
         *
         * <p> NOTE: invocation of either this method or {@link #insertBeforeTask} is mandatory,
         * as the newly created instance of {@link GradleTask} must be put
         * to a certain place in the Gradle build lifecycle.
         *
         * @param target
         *         the name of the task, serving as "after" anchor
         * @return the current instance of {@code Builder}
         */
        public Builder insertAfterTask(TaskName target) {
            checkNotNull(target, "Task before the new one");
            checkState(dependenciesRequired());
            this.previousTask = target;
            return this;
        }

        /**
         * Inserts tasks which will precede the new one.
         *
         * <p>Unlike {@link #insertAfterTask insertAfterTask()}, this method will depend
         * the new task on <b>every</b> task with such name in the project (i.e. the tasks of
         * the root project and all the subprojects).
         *
         * <p>If a certain project does not have a task with the specified name, no action is
         * performed for that project.
         *
         * <p>This method does not guarantee that the task will be included into a standard
         * Gradle build.
         *
         * <p>Invocation of this method may substitute the invocation of
         * {@link #insertAfterTask} or {@link #insertBeforeTask} if it's guaranteed that at least
         * one task with such name exists. Though the fallback is never handled and there is
         * no guarantee that the task will get into the Gradle task graph.
         *
         * @param target
         *         the name of the tasks, serving as "after" anchor
         * @return the current instance of {@code Builder}
         */
        public Builder insertAfterAllTasks(TaskName target) {
            checkNotNull(target, "Tasks before the new one");
            checkState(dependenciesRequired());
            this.previousTaskOfAllProjects = target;
            return this;
        }

        /**
         * States that the task dependencies will be added to the task later.
         *
         * <p>If this method is not called, the dependencies <strong>must</strong> be specified
         * via this builder.
         */
        public Builder allowNoDependencies() {
            checkState(previousTask == null);
            checkState(previousTaskOfAllProjects == null);
            checkState(followingTask == null);
            allowNoDependencies = true;
            return this;
        }

        /**
         * Adds the files and/or directories to the input file set for the task being built.
         *
         * <p>If none of the specified file system elements are present before the task
         * execution, the task will be marked as {@code NO-SOURCE} and skipped.
         *
         * <p>Multiple invocations appends the new files to the existing ones.
         *
         * @param inputs
         *         the task input files
         * @return the current instance of {@code Builder}
         */
        public Builder withInputFiles(FileCollection inputs) {
            checkNotNull(inputs, "Task inputs");
            this.inputs.addAll(inputs.getFiles());
            hasInputFiles = true;
            return this;
        }

        /**
         * Adds a task input property.
         *
         * <p>An input property is treated in a similar way as
         * an {@linkplain #withInputFiles input file}.
         *
         * <p>Multiple invocations of this method append new properties. If there already is
         * a property with is such a name, the value is overridden.
         *
         * @param propertyName
         *         the name of the property
         * @param value
         *         the value of the property
         * @return the current instance of {@code Builder}
         */
        public Builder withInputProperty(String propertyName, @Nullable Serializable value) {
            checkNotNull(propertyName);
            if (inputProperties == null) {
                inputProperties = new HashMap<>();
            }
            inputProperties.put(propertyName, value);
            return this;
        }

        /**
         * Adds the files and/or directories to the output file set for the task being built.
         *
         * <p>If all the files listed as output do not change since the previous run of the task,
         * the task will be marked as {@code UP-TO-DATE} and skipped.
         *
         * <p>Note that a task is not skipped if its {@link #withInputFiles inputs} are changes.
         *
         * @param outputs
         *         the task output files
         * @return the current instance of {@code Builder}
         */
        public Builder withOutputFiles(FileCollection outputs) {
            checkNotNull(outputs, "Task outputs");
            this.outputs.addAll(outputs.getFiles());
            hasOutputFiles = true;
            return this;
        }

        /**
         * Builds an instance of {@link GradleTask} and inserts it to the project,
         * if the task with the given name does not exist, specifying the task
         * lifecycle according to the "before" and "after" tasks specified in the builder.
         *
         * @param project
         *         the target Gradle project
         * @return the newly created Gradle task
         * @throws org.gradle.api.InvalidUserDataException
         *         if the task with the {@linkplain #getName() name} already exists
         */
        @CanIgnoreReturnValue
        public GradleTask applyNowTo(Project project) {
            checkNotNull(project, "Project is not specified for the new Gradle task: `%s`.", name);
            checkDependencies();
            var log = project.getLogger();
            var projectName = project.getDisplayName();
            var taskName = name.name();
            log.debug("Creating task `{}` in the project `{}`.", taskName, projectName);
            TaskProvider<Task> newTask;
            try {
                newTask = project.getTasks().register(taskName, Task.class, task -> {
                    task.doLast(action);
                });
            } catch (@SuppressWarnings("OverlyBroadCatchBlock") Exception e) {
                log.error("Failed to create task `{}` in the project `{}`.", taskName, projectName);
                throw new IllegalStateException(e);
            }
            dependTask(newTask.get(), project);
            addTaskIO(newTask.get());
            var result = new GradleTask(newTask.get(), name, project);
            return result;
        }

        private void checkDependencies() {
            if (dependenciesRequired() && !dependenciesPresent()) {
                var exceptionMsg = "Either the previous or the following task must be set. " +
                        "Call `allowNoDependencies()` to skip task dependencies setup.";
                throw new IllegalStateException(exceptionMsg);
            }
        }

        private boolean dependenciesRequired() {
            return !allowNoDependencies;
        }

        private boolean dependenciesPresent() {
            return followingTask != null
                    || previousTask != null
                    || previousTaskOfAllProjects != null;
        }

        private void dependTask(Task task, Project project) {
            if (previousTask != null) {
                task.dependsOn(previousTask.name());
            }
            if (followingTask != null) {
                var existingTasks = project.getTasks();
                existingTasks.getByName(followingTask.name())
                             .dependsOn(task);
            }
            if (previousTaskOfAllProjects != null) {
                var root = project.getRootProject();
                dependTaskOnAllProjects(task, root);
            }
        }

        private void dependTaskOnAllProjects(Task task, Project rootProject) {
            var prevTaskName = previousTaskOfAllProjects.name();
            ProjectHierarchy.applyToAll(rootProject, project -> {
                var existingTask = project.getTasks()
                                          .findByName(prevTaskName);
                if (existingTask != null) {
                    task.dependsOn(existingTask);
                }
            });
        }

        private void addTaskIO(Task task) {
            if (hasInputFiles) {
                task.getInputs()
                    .files(inputs)
                    .skipWhenEmpty()
                    .optional();
            }
            if (inputProperties != null) {
                task.getInputs()
                    .properties(inputProperties);
            }
            if (hasOutputFiles) {
                task.getOutputs()
                    .files(outputs)
                    .optional();
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("name", name)
                          .add("project", project)
                          .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, project);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (GradleTask) obj;
        return Objects.equals(this.name.name(), other.name.name())
                && Objects.equals(this.project, other.project);
    }
}
