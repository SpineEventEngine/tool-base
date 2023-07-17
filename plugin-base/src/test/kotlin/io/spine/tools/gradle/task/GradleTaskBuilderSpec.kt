/*
 * Copyright 2023, TeamDev. All rights reserved.
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
package io.spine.tools.gradle.task

import com.google.common.collect.ImmutableList
import com.google.common.truth.Truth.assertThat
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.testing.Assertions.assertIllegalState
import io.spine.tools.gradle.task.GivenTaskName.annotateProto
import io.spine.tools.gradle.task.GivenTaskName.preClean
import io.spine.tools.gradle.task.GivenTaskName.verifyModel
import io.spine.tools.gradle.task.JavaTaskName.Companion.classes
import io.spine.tools.gradle.task.JavaTaskName.Companion.compileJava
import io.spine.tools.gradle.task.ProtobufTaskName.Companion.generateProto
import io.spine.tools.gradle.task.ProtobufTaskName.Companion.generateTestProto
import io.spine.tools.gradle.testing.GradleProject
import io.spine.tools.gradle.testing.NoOp
import java.io.File
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
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
        val newTask = subProjectTasks.findByName(task.name.name())
        newTask shouldNotBe null
        val dependencies: Collection<*> = newTask!!.dependsOn
        assertThat(dependencies)
            .containsAtLeast(
                subProjectTasks.findByName(compileJava.name()),
                project.tasks.findByName(compileJava.name())
            )
    }

    @Test
    fun `create task and insert before other`() {
        GradleTask.newBuilder(verifyModel, NoOp.action())
            .insertBeforeTask(classes)
            .applyNowTo(project)
        val tasks = project.tasks
        val classes = tasks.findByName(classes.name())
        classes shouldNotBe null
        val verifyModelTask = tasks.findByName(verifyModel.name)
        classes!!.dependsOn shouldContain verifyModelTask
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
        GradleTask.newBuilder(generateTestProto, NoOp.action())
            .insertAfterAllTasks(generateProto)
            .applyNowTo(project)
        val tasks = project.tasks

        val generateProtoTask = tasks.findByName(generateProto.name())
        generateProtoTask shouldBe null
        val generateTestProtoTask = tasks.findByName(generateTestProto.name())
        generateTestProtoTask shouldNotBe null
    }

    @Test
    fun `not allow tasks without any connection to task graph`() {
        val builder = GradleTask.newBuilder(verifyModel, NoOp.action())
        assertIllegalState { builder.applyNowTo(project) }
    }

    @Test
    @DisplayName("return build task description")
    fun returnBuildTaskDescription() {
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
        val inputs = task!!.inputs
        inputs shouldNotBe null
        val inputFiles = ImmutableList.copyOf(
            inputs.files.files
        )
        inputFiles shouldHaveSize 1
        inputFiles[0]!!.canonicalFile shouldBe input.canonicalFile
    }
}
