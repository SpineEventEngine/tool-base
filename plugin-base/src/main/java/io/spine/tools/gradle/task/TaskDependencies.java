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

import org.gradle.api.Task;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskProvider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for Gradle task dependencies management.
 *
 * <p>The utilities look at the dependencies declared via
 * {@link Task#dependsOn(Object...) dependsOn()} as task names, task instances,
 * task providers, and task collections. The utilities do not create tasks.
 *
 * <p>The dependencies which Gradle infers from task inputs, such as source directories
 * {@linkplain org.gradle.api.file.ConfigurableFileCollection#builtBy(Object...) built by}
 * another task, are not taken into account. Use
 * {@link Task#getTaskDependencies() getTaskDependencies().getDependencies(task)} to obtain
 * all the dependencies of a task.
 */
public final class TaskDependencies {

    /** Prevents instantiation of this utility class. */
    private TaskDependencies() {
    }

    /**
     * Checks whether a given Gradle task depends on another Gradle task.
     */
    public static boolean dependsOn(Task task, Task ontoTask) {
        checkNotNull(task);
        checkNotNull(ontoTask);
        return dependsOn(task, ontoTask.getName());
    }

    /**
     * Checks whether a given Gradle task depends on another Gradle task with the specified name.
     */
    public static boolean dependsOn(Task task, TaskName ontoTaskWithName) {
        checkNotNull(task);
        checkNotNull(ontoTaskWithName);
        var taskName = ontoTaskWithName.name();
        return dependsOn(task, taskName);
    }

    /**
     * As long as we are dealing with Gradle Groovy-based API, we have to use `instanceof`
     * to analyze `Object[]` values returned.
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    private static boolean dependsOn(Task task, String ontoTaskWithName) {
        var dependsOn = task.getDependsOn();

        var contains = false;
        for (var anObject : dependsOn) {
            if (anObject instanceof String) {
                contains = contains || ontoTaskWithName.equals(anObject);
            }
            if (anObject instanceof Task objectAsTask) {
                contains = contains || ontoTaskWithName.equals(objectAsTask.getName());
            }
            if (anObject instanceof TaskProvider<?> provider) {
                contains = contains || ontoTaskWithName.equals(provider.getName());
            }
            if (anObject instanceof TaskCollection<?> collection) {
                contains = contains || collection.getNames()
                                                 .contains(ontoTaskWithName);
            }
        }
        return contains;
    }
}
