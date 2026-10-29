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

package io.spine.tools.gradle.task

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.spine.tools.gradle.task.TaskDependencies.dependsOn
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`TaskDependencies` should")
internal class TaskDependenciesSpec {

    private lateinit var task: Task
    private lateinit var ontoTask: Task

    @BeforeEach
    fun setUp() {
        val project = ProjectBuilder.builder().build()
        ontoTask = project.tasks.register("ontoTask").get()
        task = project.tasks.register("dependent").get()
    }

    @Test
    fun `detect a dependency on a task instance`() {
        task.dependsOn(ontoTask)

        dependsOn(task, ontoTask) shouldBe true
    }

    @Test
    fun `detect a dependency on a task name`() {
        task.dependsOn("ontoTask")

        dependsOn(task, TaskName.of("ontoTask")) shouldBe true
    }

    @Test
    fun `return 'false' when there is no dependency`() {
        dependsOn(task, ontoTask) shouldBe false
    }

    @Test
    fun `detect a dependency on a task provider without creating the task`() {
        val project = task.project
        val lazyTask = project.tasks.register("lazyTask")
        val created = mutableListOf<String>()
        project.tasks.configureEach { created += it.name }
        created.clear()

        task.dependsOn(lazyTask)

        dependsOn(task, TaskName.of("lazyTask")) shouldBe true
        dependsOn(task, TaskName.of("otherTask")) shouldBe false
        created.shouldBeEmpty()
    }

    @Test
    fun `detect a dependency on a task collection without creating the tasks`() {
        val project = task.project
        val lazyTasks = project.tasks.named { it.startsWith("lazy") }
        project.tasks.register("lazyTask")
        val created = mutableListOf<String>()
        project.tasks.configureEach { created += it.name }
        created.clear()

        task.dependsOn(lazyTasks)

        dependsOn(task, TaskName.of("lazyTask")) shouldBe true
        dependsOn(task, TaskName.of("otherTask")) shouldBe false
        created.shouldBeEmpty()
    }
}
