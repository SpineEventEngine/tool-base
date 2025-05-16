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

package io.spine.tools.gradle.project

import com.google.common.testing.NullPointerTester
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.io.Resource
import io.spine.logging.Level
import io.spine.logging.toJavaLogging
import io.spine.testing.TempDir
import io.spine.testing.logging.LoggingTest
import io.spine.tools.gradle.GradlePlugin
import io.spine.tools.gradle.PluginScript
import java.util.concurrent.atomic.AtomicBoolean
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.problems.Problem
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.ProblemReporter
import org.gradle.api.problems.ProblemSpec
import org.gradle.api.problems.internal.AdditionalDataBuilderFactory
import org.gradle.api.problems.internal.InternalProblem
import org.gradle.api.problems.internal.InternalProblemBuilder
import org.gradle.api.problems.internal.InternalProblemReporter
import org.gradle.api.problems.internal.InternalProblemSpec
import org.gradle.api.problems.internal.InternalProblems
import org.gradle.api.problems.internal.ProblemsInfrastructure
import org.gradle.api.problems.internal.ProblemsProgressEventEmitterHolder
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.reflect.Instantiator
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`PlugableProject` should")
internal class PlugableProjectSpec {
    
    private lateinit var plugableProject: PlugableProject
    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        val tempDir = TempDir.forClass(javaClass)
        project = ProjectBuilder.builder()
            .withName(PlugableProjectSpec::class.java.simpleName)
            .withProjectDir(tempDir)
            .build()
        plugableProject = PlugableProject(project)
    }

    @Test
    fun `not accept 'null' arguments`() {
        val tester = NullPointerTester()
            .setDefault(
                GradlePlugin::class.java,
                GradlePlugin.implementedIn(JavaPlugin::class.java)
            )
        tester.testAllPublicInstanceMethods(plugableProject)
        tester.testConstructors(PlugableProject::class.java, NullPointerTester.Visibility.PACKAGE)
    }

    @Test
    fun `apply a requested plugin`() {
        val plugin: GradlePlugin<*> = GradlePlugin.implementedIn(JavaPlugin::class.java)

        plugableProject.isNotApplied(plugin) shouldBe true
        plugableProject.isApplied(plugin) shouldBe false

        plugableProject.apply(plugin)

        plugableProject.isNotApplied(plugin) shouldBe false
        plugableProject.isApplied(plugin) shouldBe true
    }

    @Nested internal inner class
    LogOnDuplicate :
        LoggingTest(PlugableProject::class.java, Level.Companion.DEBUG.toJavaLogging()) {

        private lateinit var plugin: GradlePlugin<*>

        @BeforeEach
        fun setUp() {
            plugin = GradlePlugin.implementedIn(JavaPlugin::class.java)
            applyPlugin()
            interceptLogging()
        }

        private fun applyPlugin() {
            plugableProject.isApplied(plugin) shouldBe false
            plugableProject.apply(plugin)
            plugableProject.isApplied(plugin) shouldBe true
        }

        @AfterEach
        fun restoreLogger() {
            restoreLogging()
        }

        @Test
        fun `log if a plugin is applied twice`() {
            plugableProject.apply(plugin)
            plugableProject.isApplied(plugin) shouldBe true

            val assertLogRecord = assertLog().record()
            assertLogRecord.isDebug()
            assertLogRecord.hasMessageThat()
                .contains(plugin.className().value())
        }
    }

    @Test
    fun `apply Gradle scripts from classpath`() {
        // See: https://github.com/gradle/gradle/issues/31862#issuecomment-2687633265
        // and stub classes below.
        ProblemsProgressEventEmitterHolder.init(InternalProblemsStub())

        val resource = Resource.file("test-script.gradle", javaClass.classLoader)
        plugableProject.apply(PluginScript.declaredIn(resource))
        val success = project.extensions
            .extraProperties["success"]

        success shouldBe true
    }

    @Test
    fun `execute a given action if a plugin is present`() {
        val plugin = GradlePlugin.implementedIn(IdeaPlugin::class.java)
        plugableProject.apply(plugin)
        val run = AtomicBoolean(false)
        plugableProject.with(plugin) { idea: IdeaPlugin? ->
            idea shouldNotBe null
            run.set(true)
        }
        run.get() shouldBe true
    }

    @Test
    @DisplayName("execute a given action after a plugin is applied")
    fun runWhenPresent() {
        val plugin = GradlePlugin.implementedIn(IdeaPlugin::class.java)
        val run = AtomicBoolean(false)
        plugableProject.with(plugin) { idea: IdeaPlugin? ->
            idea shouldNotBe null
            run.set(true)
        }
        run.get() shouldBe false
        plugableProject.apply(plugin)
        run.get() shouldBe true
    }
}

/**
 * The stub class for workaround for
 * [this Gradle issue](https://github.com/gradle/gradle/issues/31862).
 *
 * @see <a href="https://github.com/gradle/gradle/issues/31862#issuecomment-2687633265">
 *     Workaround</a>
 */
private class InternalProblemsStub : InternalProblems {
    override fun getReporter(): ProblemReporter = notImplemented()
    override fun getInternalReporter(): InternalProblemReporter = InternalProblemReporterStub()
    override fun getInfrastructure(): ProblemsInfrastructure = notImplemented()
    override fun getProblemBuilder(): InternalProblemBuilder = notImplemented()
}

private fun notImplemented(): Nothing = TODO("Not yet implemented")

/**
 * The stub class for workaround for
 * [this Gradle issue](https://github.com/gradle/gradle/issues/31862).
 *
 * @see <a href="https://github.com/gradle/gradle/issues/31862#issuecomment-2687633265">
 *     Workaround</a>
 */
private class InternalProblemReporterStub : InternalProblemReporter {
    override fun create(problemId: ProblemId, action: Action<in ProblemSpec>): Problem =
        notImplemented()
    override fun report(problem: Problem, id: OperationIdentifier) = notImplemented()
    override fun report(problemId: ProblemId, spec: Action<in ProblemSpec>) = notImplemented()
    override fun report(problem: Problem) = notImplemented()
    override fun report(problems: MutableCollection<out Problem>) = notImplemented()

    override fun throwing(
        exception: Throwable,
        problemId: ProblemId,
        spec: Action<in ProblemSpec>
    ): RuntimeException = notImplemented()

    override fun throwing(exception: Throwable, problem: Problem): RuntimeException =
        notImplemented()

    override fun throwing(
        exception: Throwable,
        problems: MutableCollection<out Problem>
    ): RuntimeException = notImplemented()

    override fun internalCreate(action: Action<in InternalProblemSpec>): InternalProblem =
        notImplemented()
}
