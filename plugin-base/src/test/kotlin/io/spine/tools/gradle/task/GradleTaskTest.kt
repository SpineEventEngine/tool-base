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

import com.google.common.testing.EqualsTester
import com.google.common.testing.NullPointerTester
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`GradleTask` should")
@Suppress("DEPRECATION") // Covers the deprecated `GradleTask` until it is removed.
class GradleTaskTest {

    @Test
    fun `handle 'null' arguments in static methods`() {
        NullPointerTester().testAllPublicStaticMethods(GradleTask::class.java)
    }

    @Test
    fun `create an instance from an existing Gradle task`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("existingTask").get()

        val gradleTask = GradleTask.from(task)

        gradleTask.let {
            it.task shouldBe task
            it.taskProvider.get() shouldBe task
            it.name.name() shouldBe "existingTask"
        }
    }

    @Test
    fun `provide a string representation`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("aTask").get()

        GradleTask.from(task).toString() shouldContain "GradleTask"
    }

    @Test
    fun `support equality`() {
        val project = ProjectBuilder.builder().build()
        val taskA = project.tasks.register("taskA").get()
        val taskB = project.tasks.register("taskB").get()

        EqualsTester()
            .addEqualityGroup(GradleTask.from(taskA), GradleTask.from(taskA))
            .addEqualityGroup(GradleTask.from(taskB))
            .testEquals()
    }

    @Test
    fun `allow setting 'group' and 'description'`() {
        val project = ProjectBuilder.builder().build()
        val taskName = TaskName.of("testTask")
        val group = SpineTaskGroup.name
        val description = "Test description"

        val gradleTask = GradleTask.newBuilder(taskName) { }
            .withGroup(group)
            .withDescription(description)
            .allowNoDependencies()
            .applyNowTo(project)

        gradleTask.task.group shouldBe group
        gradleTask.task.description shouldBe description

        gradleTask.group shouldBe group
        gradleTask.description shouldBe description
    }
}
