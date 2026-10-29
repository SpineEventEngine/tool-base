/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package io.spine.tools.gradle.task;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
 * Utility wrapper around the Gradle tasks registered.
 *
 * <p>Instantiated via {@link Builder}, forces the new task to be added to
 * the Gradle build lifecycle.
 *
 * <p>The wrapper refers to the task via its {@link TaskProvider}. Therefore, Gradle creates
 * the task only if the build needs it, or if {@link #getTask()} is called.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") /* Instantiated via Builder. */
public final class GradleTask {

    private final TaskProvider<Task> task;
    private final TaskName name;
    private final Project project;

    private GradleTask(TaskProvider<Task> task, TaskName name, Project project) {
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
        var provider = project.getTasks()
                              .named(task.getName());
        return new GradleTask(provider, taskName, project);
    }

    /**
     * Obtains the Gradle task itself.
     *
     * <p>Gradle creates the task, if it is not created yet.
     * Use {@link #getTaskProvider()} to configure the task without creating it.
     */
    public Task getTask() {
        return task.get();
    }

    /**
     * Obtains the provider of the Gradle task.
     *
     * <p>Unlike {@link #getTask()}, this method does not create the task.
     */
    public TaskProvider<Task> getTaskProvider() {
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
     * Obtains the group of the task, or {@code null} if the group is not set.
     *
     * <p>Gradle creates the task, if it is not created yet.
     */
    public @Nullable String getGroup() {
        return getTask().getGroup();
    }

    /**
     * Obtains the description of the task, or {@code null} if the description is not set.
     *
     * <p>Gradle creates the task, if it is not created yet.
     */
    public @Nullable String getDescription() {
        return getTask().getDescription();
    }

    /**
     * A builder for {@link GradleTask}.
     *
     * <p>NOTE: unlike most classes following the {@code Builder} pattern,
     * this one provides {@link #applyNowTo(Project)} method instead of
     * {@code build(..)}. This is done to add some additional semantics to
     * such an irreversible action like this.
     */
    public static final class Builder {
        private final TaskName name;
        private final Action<Task> action;

        private @Nullable TaskName previousTask;
        private @Nullable TaskName previousTaskOfAllProjects;
        private @Nullable TaskName followingTask;

        private @Nullable String group;
        private @Nullable String description;

        private boolean allowNoDependencies;

        private boolean hasInputFiles = false;
        private final Set<File> inputs = new HashSet<>();
        private @Nullable Map<String, @Nullable Object> inputProperties;
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
         * performed for that project. Only the tasks registered by the time of
         * the {@link #applyNowTo(Project) applyNowTo()} call are taken into account.
         * These tasks are not created by the call.
         *
         * <p>This method does not guarantee that the task will be included in a standard
         * Gradle build.
         *
         * <p>Invocation of this method may substitute the invocation of
         * {@link #insertAfterTask} or {@link #insertBeforeTask} if it's guaranteed that at least
         * one task with such a name exists. Though the fallback is never handled, and there is
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
         * Sets the group for the task being built.
         *
         * @param group
         *         the task group
         * @return the current instance of {@code Builder}
         */
        public Builder withGroup(String group) {
            checkNotNull(group);
            this.group = group;
            return this;
        }

        /**
         * Sets the description for the task being built.
         *
         * @param description
         *         the task description
         * @return the current instance of {@code Builder}
         */
        public Builder withDescription(String description) {
            checkNotNull(description);
            this.description = description;
            return this;
        }

        /**
         * Adds the files and/or directories to the input file set for the task being built.
         *
         * <p>If none of the specified file system elements are present before the task
         * execution, the task will be marked as {@code NO-SOURCE} and skipped.
         *
         * <p>Multiple invocations append the new files to the existing ones.
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
         * <p>Multiple invocations of this method append new properties.
         * If there already is a property with such a name, the value is overridden.
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
         * Registers the task with the given name in the project, specifying the task
         * lifecycle according to the "before" and "after" tasks specified in the builder.
         *
         * <p>The method does not create the task, nor the tasks it depends on.
         * Gradle creates the new task only if the build needs it, and configures it at
         * that time according to the state of this builder at the moment of this call.
         * Later changes to the builder do not affect the registered task.
         *
         * @param project
         *         the target Gradle project
         * @return the newly registered Gradle task
         * @throws IllegalStateException
         *         if the task with the {@linkplain #getName() name} already exists
         * @throws org.gradle.api.UnknownTaskException
         *         if the {@linkplain #insertBeforeTask(TaskName) following task}
         *         is not registered in the project
         */
        @CanIgnoreReturnValue
        public GradleTask applyNowTo(Project project) {
            checkNotNull(project, "Project is not specified for the new Gradle task: `%s`.", name);
            checkDependencies();
            var log = project.getLogger();
            var projectName = project.getDisplayName();
            var taskName = name.name();
            log.debug("Registering task `{}` in the project `{}`.", taskName, projectName);
            var setup = new TaskSetup(this, previousTasksOfAllProjects(project));
            // Obtain the following task before registering the new one, so that
            // a missing following task does not leave the new task registered.
            @Nullable TaskProvider<Task> following = followingTask != null
                                                     ? project.getTasks()
                                                              .named(followingTask.name())
                                                     : null;
            TaskProvider<Task> newTask;
            try {
                newTask = project.getTasks()
                                 .register(taskName, Task.class, setup);
            } catch (@SuppressWarnings("OverlyBroadCatchBlock") Exception e) {
                log.error("Failed to register task `{}` in the project `{}`.",
                          taskName, projectName);
                throw new IllegalStateException(e);
            }
            if (following != null) {
                following.configure(task -> task.dependsOn(newTask));
            }
            var result = new GradleTask(newTask, name, project);
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

        /**
         * Obtains the providers of the tasks named as
         * the {@linkplain #insertAfterAllTasks(TaskName) previous task of all projects}
         * in the hierarchy of the given project.
         *
         * <p>The method looks only at the names of the tasks registered by the time of
         * this call, so it does not create the tasks.
         *
         * @return the providers, or an empty list if the previous task of all projects
         *         is not set
         */
        private ImmutableList<TaskProvider<Task>> previousTasksOfAllProjects(Project project) {
            if (previousTaskOfAllProjects == null) {
                return ImmutableList.of();
            }
            var prevTaskName = previousTaskOfAllProjects.name();
            var result = ImmutableList.<TaskProvider<Task>>builder();
            ProjectHierarchy.applyToAll(project.getRootProject(), p -> {
                var tasks = p.getTasks();
                if (tasks.getNames().contains(prevTaskName)) {
                    result.add(tasks.named(prevTaskName));
                }
            });
            return result.build();
        }
    }

    /**
     * Configures a task registered by {@link Builder#applyNowTo(Project)}.
     *
     * <p>Gradle runs this action when it creates the task, which may happen long after
     * the builder is applied, if at all. Therefore, the action holds a copy of
     * the builder state rather than refers to the builder.
     */
    private static final class TaskSetup implements Action<Task> {

        private final Action<Task> action;
        private final @Nullable String group;
        private final @Nullable String description;
        private final @Nullable TaskName previousTask;
        private final ImmutableList<TaskProvider<Task>> previousTasksOfAllProjects;
        private final @Nullable ImmutableSet<File> inputs;
        private final @Nullable Map<String, @Nullable Object> inputProperties;
        private final @Nullable ImmutableSet<File> outputs;

        private TaskSetup(Builder builder,
                          ImmutableList<TaskProvider<Task>> previousTasksOfAllProjects) {
            this.action = builder.action;
            this.group = builder.group;
            this.description = builder.description;
            this.previousTask = builder.previousTask;
            this.previousTasksOfAllProjects = previousTasksOfAllProjects;
            this.inputs = builder.hasInputFiles ? ImmutableSet.copyOf(builder.inputs) : null;
            this.inputProperties = builder.inputProperties != null
                                   ? new HashMap<>(builder.inputProperties)
                                   : null;
            this.outputs = builder.hasOutputFiles ? ImmutableSet.copyOf(builder.outputs) : null;
        }

        @Override
        public void execute(Task task) {
            if (group != null) {
                task.setGroup(group);
            }
            if (description != null) {
                task.setDescription(description);
            }
            task.doLast(action);
            dependTask(task);
            addTaskIO(task);
        }

        private void dependTask(Task task) {
            if (previousTask != null) {
                task.dependsOn(previousTask.name());
            }
            for (var previous : previousTasksOfAllProjects) {
                task.dependsOn(previous);
            }
        }

        private void addTaskIO(Task task) {
            if (inputs != null) {
                task.getInputs()
                    .files(inputs)
                    .skipWhenEmpty()
                    .optional();
            }
            if (inputProperties != null) {
                task.getInputs()
                    .properties(inputProperties);
            }
            if (outputs != null) {
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
    public boolean equals(@Nullable Object obj) {
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
