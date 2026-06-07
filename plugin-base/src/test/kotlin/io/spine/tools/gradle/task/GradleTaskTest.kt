/*
 * Copyright 2026, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.task

import com.google.common.testing.EqualsTester
import com.google.common.testing.NullPointerTester
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`GradleTask` should")
class GradleTaskTest {

    @Test
    fun `handle 'null' arguments in static methods`() {
        NullPointerTester().testAllPublicStaticMethods(GradleTask::class.java)
    }

    @Test
    fun `create an instance from an existing Gradle task`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.create("existingTask")

        val gradleTask = GradleTask.from(task)

        gradleTask.let {
            it.task shouldBe task
            it.name.name() shouldBe "existingTask"
        }
    }

    @Test
    fun `provide a string representation`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.create("aTask")

        GradleTask.from(task).toString() shouldContain "GradleTask"
    }

    @Test
    fun `support equality`() {
        val project = ProjectBuilder.builder().build()
        val taskA = project.tasks.create("taskA")
        val taskB = project.tasks.create("taskB")

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
