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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.testing.Assertions.assertIllegalState
import io.spine.tools.gradle.task.GivenTaskName.annotateProto
import io.spine.tools.gradle.task.GivenTaskName.preClean
import io.spine.tools.gradle.task.GivenTaskName.verifyModel
import io.spine.tools.gradle.task.JavaTaskName.Companion.classes
import io.spine.tools.gradle.task.JavaTaskName.Companion.compileJava
import io.spine.tools.gradle.testing.GradleProject
import io.spine.tools.gradle.testing.NoOp
import java.io.File
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests [GradleTask.Builder].
 */
@DisplayName("`GradleTask.Builder` should")
internal class GradleTaskBuilderSpec {

    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        project = ProjectBuilder.builder().build().also {
            it.pluginManager.apply(GradleProject.javaPlugin)
        }
    }

    @Test
    fun `create task dependant on all tasks of given name`() {
        val subProject = ProjectBuilder.builder()
            .withParent(project)
            .build()
        subProject.pluginManager
            .apply(GradleProject.javaPlugin)
        val task = GradleTask.newBuilder(annotateProto, NoOp.action())
            .insertAfterAllTasks(compileJava)
            .applyNowTo(subProject)
        val subProjectTasks = subProject.tasks
        val newTask = subProjectTasks.getByName(task.name.name())
        val dependencies = newTask.dependencies()
        dependencies shouldContain subProjectTasks.getByName(compileJava.name())
        dependencies shouldContain project.tasks.getByName(compileJava.name())
    }

    @Test
    fun `create task and insert before other`() {
        GradleTask.newBuilder(verifyModel, NoOp.action())
            .insertBeforeTask(classes)
            .applyNowTo(project)
        val tasks = project.tasks
        val classes = tasks.getByName(classes.name())
        val verifyModelTask = tasks.getByName(verifyModel.name)
        classes.dependencies() shouldContain verifyModelTask
        // The dependency is declared via a task provider, which the utility recognizes.
        TaskDependencies.dependsOn(classes, verifyModel) shouldBe true
    }

    @Test
    fun `create task and insert after other`() {
        GradleTask.newBuilder(verifyModel, NoOp.action())
            .insertAfterTask(compileJava)
            .applyNowTo(project)
        val tasks = project.tasks
        val compileJavaTask = tasks.findByName(compileJava.name())
        compileJavaTask shouldNotBe null
        val verifyModelTask = tasks.findByName(verifyModel.name)
        verifyModelTask shouldNotBe null
        verifyModelTask!!.dependsOn shouldContain compileJavaTask!!.name
    }

    @Test
    fun `ignore task dependency if no such task found`() {
        val generateTestProto = TaskName.of("generateTestProto")
        val generateProto = TaskName.of("generateProto")
        GradleTask.newBuilder(generateTestProto, NoOp.action())
            .insertAfterAllTasks(generateProto)
            .applyNowTo(project)
        val tasks = project.tasks

        val generateProtoTask = tasks.findByName(generateProto.name())
        generateProtoTask shouldBe null
        tasks.getByName(generateTestProto.name()).dependencies().shouldBeEmpty()
    }

    @Test
    fun `not allow tasks without any connection to task graph`() {
        val builder = GradleTask.newBuilder(verifyModel, NoOp.action())
        assertIllegalState { builder.applyNowTo(project) }
    }

    @Test
    fun `wrap a task registration failure into an 'IllegalStateException'`() {
        // Reserve the task name up front so that the builder's own
        // `register(...)` call fails with a duplicate-name error, exercising
        // the failure-handling branch of `applyNowTo()`.
        project.tasks.register(preClean.name)

        val builder = GradleTask.newBuilder(preClean, NoOp.action())
            .insertBeforeTask(BaseTaskName.clean)

        val error = shouldThrow<IllegalStateException> {
            builder.applyNowTo(project)
        }
        error.cause shouldNotBe null
    }

    @Test
    fun `return build task description`() {
        val desc = GradleTask.newBuilder(preClean, NoOp.action())
            .insertBeforeTask(BaseTaskName.clean)
            .applyNowTo(project)

        desc.name shouldBe preClean
        desc.project shouldBe project
    }

    @Test
    fun `create task with given inputs`() {
        val input = File(".").absoluteFile
        val files = project.layout.files(input)
        GradleTask.newBuilder(preClean, NoOp.action())
            .insertBeforeTask(BaseTaskName.clean)
            .withInputFiles(files)
            .applyNowTo(project)

        val task = project.tasks.findByPath(preClean.name)
        task shouldNotBe null
        val inputFiles = task!!.inputs.files.files.toList()
        inputFiles shouldHaveSize 1
        inputFiles[0].canonicalFile shouldBe input.canonicalFile
    }

    @Nested
    inner class `'withInputProperty'` {

        @Test
        fun `register input property on task`() {
            GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(BaseTaskName.clean)
                .withInputProperty("propName", "propValue")
                .applyNowTo(project)

            val task = project.tasks.findByPath(preClean.name)
            task shouldNotBe null
            task!!.inputs.properties["propName"] shouldBe "propValue"
        }

        @Test
        fun `append multiple input properties on separate calls`() {
            GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(BaseTaskName.clean)
                .withInputProperty("first", "alpha")
                .withInputProperty("second", "beta")
                .applyNowTo(project)

            val task = project.tasks.findByPath(preClean.name)
            task shouldNotBe null
            val properties = task!!.inputs.properties
            properties["first"] shouldBe "alpha"
            properties["second"] shouldBe "beta"
        }

        @Test
        fun `override input property value when called with same name`() {
            GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(BaseTaskName.clean)
                .withInputProperty("key", "original")
                .withInputProperty("key", "overridden")
                .applyNowTo(project)

            val task = project.tasks.findByPath(preClean.name)
            task shouldNotBe null
            task!!.inputs.properties["key"] shouldBe "overridden"
        }

        @Test
        fun `accept null as input property value`() {
            GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(BaseTaskName.clean)
                .withInputProperty("nullProp", null)
                .applyNowTo(project)

            val task = project.tasks.findByPath(preClean.name)
            task shouldNotBe null
            task!!.inputs.properties shouldContainKey "nullProp"
        }
    }

    @Nested
    inner class `'withOutputFiles'` {

        @Test
        fun `register output files on task`() {
            val output = File(".").absoluteFile
            val files = project.layout.files(output)
            GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(BaseTaskName.clean)
                .withOutputFiles(files)
                .applyNowTo(project)

            val task = project.tasks.findByPath(preClean.name)
            task shouldNotBe null
            val outputFiles = task!!.outputs.files.files.toList()
            outputFiles shouldHaveSize 1
            outputFiles[0].canonicalFile shouldBe output.canonicalFile
        }

        @Test
        fun `append output files on multiple calls`() {
            val firstOutput = File(".").absoluteFile
            val secondOutput = File("..").absoluteFile
            GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(BaseTaskName.clean)
                .withOutputFiles(project.layout.files(firstOutput))
                .withOutputFiles(project.layout.files(secondOutput))
                .applyNowTo(project)

            val task = project.tasks.findByPath(preClean.name)
            task shouldNotBe null
            val outputFiles = task!!.outputs.files.files
            val canonicalPaths = outputFiles.map { it.canonicalFile }
            canonicalPaths shouldContain firstOutput.canonicalFile
            canonicalPaths shouldContain secondOutput.canonicalFile
        }
    }

    @Test
    fun `allow creating task with no dependencies if explicitly permitted`() {
        val standaloneProject = ProjectBuilder.builder().build()
        val taskName = TaskName.of("taskWithNoDependencies")

        shouldThrow<IllegalStateException> {
            GradleTask
                .newBuilder(taskName) { _: Task -> }
                .applyNowTo(standaloneProject)
        }

        val task = GradleTask
            .newBuilder(taskName) { _: Task -> }
            .allowNoDependencies()
            .applyNowTo(standaloneProject)

        task.name shouldBe taskName
    }

    @Nested
    inner class `register the task lazily` {

        private val created = mutableListOf<String>()

        @BeforeEach
        fun recordCreatedTasks() {
            project.tasks.configureEach { created += it.path }
            // `configureEach` runs at once for the tasks created earlier, so start afresh.
            created.clear()
        }

        @Test
        fun `creating neither the task nor the tasks it is inserted between`() {
            val subProject = ProjectBuilder.builder()
                .withParent(project)
                .build()
            subProject.pluginManager.apply(GradleProject.javaPlugin)
            subProject.tasks.configureEach { created += it.path }
            created.clear()

            GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(BaseTaskName.clean)
                .insertAfterTask(compileJava)
                .insertAfterAllTasks(classes)
                .applyNowTo(subProject)

            created.shouldBeEmpty()
        }

        @Test
        fun `creating the task on demand`() {
            val task = GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(BaseTaskName.clean)
                .applyNowTo(project)

            task.taskProvider.name shouldBe preClean.name
            created.shouldBeEmpty()

            task.task.name shouldBe preClean.name
            created shouldContainExactly listOf(":${preClean.name}")
        }

        @Test
        fun `ignoring changes to the builder made after it is applied`() {
            val builder = GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(BaseTaskName.clean)
                .withDescription("Original description")
            builder.applyNowTo(project)

            builder.withDescription("Changed description")
                .withInputProperty("late", "value")
                .withOutputFiles(project.layout.files(File(".").absoluteFile))

            val task = project.tasks.getByName(preClean.name)
            task.description shouldBe "Original description"
            task.inputs.properties shouldNotContainKey "late"
            task.outputs.files.shouldBeEmpty()
        }

        @Test
        fun `depending only on tasks of all projects registered before it`() {
            val subProject = ProjectBuilder.builder()
                .withParent(project)
                .build()
            val rootTask = project.tasks.register(verifyModel.name)
            val task = GradleTask.newBuilder(annotateProto, NoOp.action())
                .insertAfterAllTasks(verifyModel)
                .applyNowTo(subProject)
            subProject.tasks.register(verifyModel.name)

            created.shouldBeEmpty()
            task.task.dependencies() shouldContainExactly setOf(rootTask.get())
        }

        @Test
        fun `not registering the task if the following task is not registered`() {
            val builder = GradleTask.newBuilder(preClean, NoOp.action())
                .insertBeforeTask(TaskName.of("missingTask"))

            shouldThrow<UnknownTaskException> {
                builder.applyNowTo(project)
            }
            project.tasks.names shouldNotContain preClean.name
        }
    }
}

/**
 * Obtains the tasks this task depends on, resolving the dependencies
 * declared via names, providers, and task collections.
 */
private fun Task.dependencies(): Set<Task> =
    taskDependencies.getDependencies(this)
