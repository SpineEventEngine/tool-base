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

package io.spine.tools.gradle.testing

import com.google.errorprone.annotations.CanIgnoreReturnValue
import io.spine.tools.gradle.task.TaskName
import io.spine.tools.gradle.testing.GradleProject.Companion.setupAt
import java.io.File
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure

/**
 * Allows configuring a Gradle project for testing needs.
 *
 * The project operates in the [given directory][setupAt] and allows executing Gradle tasks.
 *
 * The configuration of the project starts with the call to the [setupAt] method, which accepts
 * the directory where the project would be placed. The method returns [GradleProjectSetup]
 * which is used for tuning the project to be created.
 *
 * ## TestKit worker coverage
 *
 * Plugin code exercised through Gradle TestKit runs in a separate worker JVM, which
 * JaCoCo/Kover do not instrument by default, so that out-of-process execution is
 * otherwise not credited to coverage. This library attaches the JaCoCo agent to the
 * worker JVM automatically — for projects run via this class as well as via
 * [runGradleBuild] — but only when the test JVM is started with two system
 * properties, which the consuming build is responsible for setting:
 *
 *  - `io.spine.tools.gradle.testkit.coverage.agent` — the absolute path to the
 *    standalone JaCoCo agent JAR, that is, the artifact
 *    `org.jacoco:org.jacoco.agent:<version>:runtime`.
 *  - `io.spine.tools.gradle.testkit.coverage.execDir` — the directory into which the
 *    worker should write its execution data. The harness creates a single
 *    per-module `testkit.exec` file inside it.
 *
 * When the properties are absent the harness is a no-op, so ordinary runs and
 * consumers that do not opt in are unaffected.
 *
 * The snippet below is a self-contained Gradle (Kotlin DSL) setup that a module
 * running TestKit-based tests can copy into its `build.gradle.kts`. Pin the agent
 * to the same JaCoCo version your coverage tooling uses.
 *
 * ```kotlin
 * import java.util.concurrent.atomic.AtomicBoolean
 *
 * // A resolvable, non-consumable configuration holding the standalone agent JAR.
 * val testKitJacocoAgent: Configuration by configurations.creating {
 *     isCanBeConsumed = false
 *     isCanBeResolved = true
 * }
 * dependencies {
 *     testKitJacocoAgent("org.jacoco:org.jacoco.agent:0.8.15:runtime")
 * }
 *
 * val agentJar = testKitJacocoAgent.elements.map { it.single().asFile.absolutePath }
 * val execDir = layout.buildDirectory.dir("jacoco-testkit")
 *
 * // Wipe the exec directory at most once per build, from the first Test task
 * // that actually executes. The workers append to a single per-module exec
 * // file, so several TestKit tasks accumulate into it instead of erasing one
 * // another. (A `dependsOn` clean task would also delete the file on up-to-date
 * // or cached runs that never regenerate it.)
 * val cleaned = AtomicBoolean(false)
 *
 * tasks.withType<Test>().configureEach {
 *     inputs.files(testKitJacocoAgent)
 *     // Worker `.exec` data is flushed out-of-process on daemon shutdown, after
 *     // the task action, so it cannot be a declared output: a cache hit would
 *     // skip execution and drop the coverage.
 *     outputs.cacheIf("TestKit worker coverage cannot be a declared output") { false }
 *     doFirst {
 *         val dir = execDir.get().asFile
 *         if (cleaned.compareAndSet(false, true)) {
 *             dir.deleteRecursively() // Drop stale worker coverage from a previous run.
 *         }
 *         dir.mkdirs()
 *         systemProperty("io.spine.tools.gradle.testkit.coverage.agent", agentJar.get())
 *         systemProperty("io.spine.tools.gradle.testkit.coverage.execDir", dir.absolutePath)
 *     }
 * }
 * ```
 *
 * The agent writes binary JaCoCo execution data (the `testkit.exec` file); feed
 * that file into your coverage report (JaCoCo or Kover) as an additional binary
 * report, so the out-of-process worker coverage is merged with the in-process
 * test coverage.
 *
 * Within the SpineEventEngine organisation this wiring is already provided by the
 * `io.spine.gradle.testing.enableTestKitCoverage` Gradle extension that ships in
 * `config`'s `buildSrc`, and the produced files are merged into Kover reports by
 * `KoverConfig`. Consumers outside that setup can use the snippet above instead.
 *
 * @see GradleProjectSetup
 */
public class GradleProject internal constructor(setup: GradleProjectSetup) {

    /**
     * The name of the project directory which is used for identifying the project
     * during debug time.
     *
     * If the project was loaded from resources, this value contains the name of
     * the resource directory. Otherwise, it contains the short name of a temporary
     * directory used for running the project.
     *
     * @see toString
     */
    public val directoryName: String = setup.resourceDir ?: setup.projectDir.name

    /**
     * The arguments passed to [runner].
     */
    private val arguments: RunnerArguments = setup.arguments

    /**
     * The runner for executing tasks.
     */
    public val runner: GradleRunner by lazy {
        val runner = GradleRunner.create()
            .withProjectDir(setup.projectDir)
            .withDebug(setup.debug)
        val sharedTestKitDir = if (setup.useSharedTestKit) {
            RootProject.testKitTempDir().toFile().apply { mkdirs() }
        } else {
            null
        }
        if (sharedTestKitDir != null) {
            runner.withTestKitDir(sharedTestKitDir)
        }
        runner.enableTestKitCoverage(sharedTestKitDir)
        runner
    }

    init {
        if (setup.additionalClasspathElements.isNotEmpty()) {
            val pluginClasspath = runner.withPluginClasspath().pluginClasspath
            runner.withPluginClasspath(setup.additionalClasspathElements + pluginClasspath)
        } else if (setup.withDefaultPluginClasspath) {
            runner.withPluginClasspath()
        }
        if (setup.environment != null) {
            runner.withEnvironment(setup.environment)
        }
        writeSources(setup)
        replaceTokens(setup)
    }

    @Suppress("ConstPropertyName") // https://bit.ly/kotlin-prop-names
    public companion object {

        /**
         * The ID of a Java Gradle plugin.
         */
        public const val javaPlugin: String = "java"

        /**
         * The ID of Protobuf Gradle Plugin.
         */
        public const val protobufPlugin: String = "com.google.protobuf"

        /**
         * Starts creation of a new the project.
         *
         * @param projectDir
         *          file system directory under which the project will be created
         */
        @JvmStatic
        public fun setupAt(projectDir: File): GradleProjectSetup {
            return GradleProjectSetup(projectDir)
        }

        private fun writeSources(setup: GradleProjectSetup) {
            val sources = Sources(setup)
            sources.write()
        }

        /**
         * Uses the pre-configured [replacements][GradleProjectSetup.replacements] and replaces
         * the tokens in all files of the [projectDir] and its subfolders.
         *
         * The contents of `projectDir/buildSrc` folder are ignored in this process — as these files
         * hardly ever may contain the tokenized values.
         */
        private fun replaceTokens(setup: GradleProjectSetup) {
            val buildSrcDir = setup.projectDir.resolve("buildSrc")
            setup.projectDir
                .walk()
                .filter { f -> !f.isDirectory }
                .filter { f -> !f.isIn(buildSrcDir) }
                .forEach { f ->
                    setup.replacements.forEach { r ->
                        r.replaceIn(f)
                    }
                }
        }
    }

    /**
     * The directory of this project.
     */
    public val projectDir: File = runner.projectDir

    /**
     * Executes the task with the given name.
     *
     * @throws IllegalStateException when the build fails with
     * [UnexpectedBuildFailure][org.gradle.testkit.runner.UnexpectedBuildFailure] citing
     * the output of [BuildResult] in its message.
     */
    @CanIgnoreReturnValue
    public fun executeTask(task: TaskName): BuildResult {
        val run = prepareRun(task)
        try {
            return run.build()
        } catch (e: UnexpectedBuildFailure) {
            val output = e.buildResult.output
            val message = "Unexpected build failure." + """

                Project: `${projectDir.path}`
                Task: `${task.name()}`
                Build result output =====>
                """.trimIndent() +
                output +
                """
                <===== Build result output end.
                """.trimIndent()
            throw IllegalStateException(message, e)
        }
    }

    /**
     * Executes the task with the given name and returns the failed build result.
     */
    @CanIgnoreReturnValue
    public fun executeAndFail(task: TaskName): BuildResult {
        val run = prepareRun(task)
        return run.buildAndFail()
    }

    private fun prepareRun(taskName: TaskName): GradleRunner {
        val args = arguments.forTask(taskName)
        return runner.withArguments(args)
    }

    /**
     * Returns [directoryName].
     */
    override fun toString(): String = directoryName
}

/**
 * Tells whether the file resides in the [folder].
 *
 * If the [folder] is `null`, returns `false`.
 */
private fun File.isIn(folder: File?) =
    if (folder == null) {
        false
    } else {
        this.absolutePath.startsWith(folder.absolutePath)
    }
