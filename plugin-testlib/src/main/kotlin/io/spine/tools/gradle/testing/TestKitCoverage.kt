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

import java.io.File
import org.gradle.testkit.runner.GradleRunner

/**
 * Attaches the JaCoCo coverage agent to the worker JVM spawned by this runner,
 * so that plugin code executed out-of-process via TestKit is credited to coverage.
 *
 * Does nothing unless the test JVM was started with the [AGENT_PROPERTY] and
 * [EXEC_DIR_PROPERTY] system properties. This keeps the harness a no-op for
 * ordinary runs and for consumers that do not opt in.
 *
 * When enabled, the method writes a `gradle.properties` carrying a `-javaagent:…`
 * argument into the TestKit directory (the Gradle user home of the worker build),
 * and points the runner at that directory. The JaCoCo agent appends to a single
 * per-module [execution-data file][EXEC_FILE]; on worker daemon shutdown — which
 * happens after the tests complete and before the coverage report task runs — the
 * data is flushed to disk.
 *
 * ## Enabling TestKit coverage from your build
 *
 * The harness reads two system properties; supplying them on the test JVM is the
 * responsibility of the build that uses `plugin-testlib`:
 *
 *  - [AGENT_PROPERTY] (`io.spine.tools.gradle.testkit.coverage.agent`) — the
 *    absolute path to the standalone JaCoCo agent JAR, that is, the artifact
 *    `org.jacoco:org.jacoco.agent:<version>:runtime`.
 *  - [EXEC_DIR_PROPERTY] (`io.spine.tools.gradle.testkit.coverage.execDir`) — the
 *    directory into which the worker should write its execution data. The harness
 *    creates the per-module [EXEC_FILE] inside it.
 *
 * The snippet below is a self-contained Gradle (Kotlin DSL) setup that a module
 * running TestKit-based tests can copy into its `build.gradle.kts`. Pin the agent
 * to the same JaCoCo version your coverage tooling uses.
 *
 * ```kotlin
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
 * tasks.withType<Test>().configureEach {
 *     inputs.files(testKitJacocoAgent)
 *     doFirst {
 *         val dir = execDir.get().asFile
 *         dir.deleteRecursively() // Drop stale worker coverage from a previous run.
 *         dir.mkdirs()
 *         systemProperty("io.spine.tools.gradle.testkit.coverage.agent", agentJar.get())
 *         systemProperty("io.spine.tools.gradle.testkit.coverage.execDir", dir.absolutePath)
 *     }
 * }
 * ```
 *
 * The agent writes binary JaCoCo execution data (an [EXEC_FILE]); feed that file
 * into your coverage report (JaCoCo or Kover) as an additional binary report, so
 * the out-of-process worker coverage is merged with the in-process test coverage.
 *
 * Within the SpineEventEngine organisation this wiring is already provided by the
 * `io.spine.gradle.testing.enableTestKitCoverage` Gradle extension that ships in
 * `config`'s `buildSrc`, and the produced files are merged into Kover reports by
 * `KoverConfig`. Consumers outside that setup can use the snippet above instead.
 *
 * @param preferredTestKitDir The TestKit directory the caller already intends to
 *   use (e.g. a [shared one][GradleProjectSetup.withSharedTestKitDirectory]), or
 *   `null` to let this method pick the per-module [TESTKIT_HOME_DIR].
 */
internal fun GradleRunner.enableTestKitCoverage(preferredTestKitDir: File? = null) {
    val agentJar = System.getProperty(AGENT_PROPERTY) ?: return
    val execDirPath = System.getProperty(EXEC_DIR_PROPERTY) ?: return

    val execDir = File(execDirPath).apply { mkdirs() }
    val execFile = File(execDir, EXEC_FILE)
    val testKitDir = (preferredTestKitDir ?: File(execDir.parentFile, TESTKIT_HOME_DIR))
        .apply { mkdirs() }

    val jvmArg = "-javaagent:${agentJar.forProperties()}=" +
            "destfile=${execFile.invariantSeparatorsPath}," +
            "append=true,dumponexit=true,output=file,jmx=false"
    File(testKitDir, "gradle.properties")
        .writeText("org.gradle.jvmargs=$jvmArg\n")

    withTestKitDir(testKitDir)
}

/**
 * Normalizes a file-system path for safe inclusion into a `*.properties` file
 * by using forward slashes regardless of the host platform.
 */
private fun String.forProperties(): String = File(this).invariantSeparatorsPath

/**
 * The name of the system property carrying the absolute path to the JaCoCo
 * agent JAR to attach to a Gradle TestKit worker JVM.
 *
 * The consuming build is expected to set this property on the test JVM. Within
 * the SpineEventEngine organisation it is set by the
 * `io.spine.gradle.testing.enableTestKitCoverage` extension in `config`'s
 * `buildSrc`, where the constant is duplicated (the `buildSrc` and the production
 * source sets cannot share code). Keep the two values in sync.
 */
private const val AGENT_PROPERTY: String =
    "io.spine.tools.gradle.testkit.coverage.agent"

/**
 * The name of the system property carrying the absolute path to the directory
 * where the TestKit worker should write its JaCoCo execution data.
 *
 * Set by the consuming build on the test JVM. Within the SpineEventEngine
 * organisation this is done by the `io.spine.gradle.testing.enableTestKitCoverage`
 * extension in `config`'s `buildSrc`; keep the two values in sync.
 */
private const val EXEC_DIR_PROPERTY: String =
    "io.spine.tools.gradle.testkit.coverage.execDir"

/**
 * The name of the JaCoCo execution-data file written by TestKit workers.
 *
 * A single file per module is reused across all the test cases of the module:
 * the JaCoCo agent appends to it, so the worker coverage of all the cases is
 * accumulated. `KoverConfig` (in `buildSrc`) feeds the file
 * into the Kover reports.
 */
private const val EXEC_FILE: String = "testkit.exec"

/**
 * The name of the directory, placed next to the [execution-data directory][EXEC_DIR_PROPERTY],
 * that serves as the dedicated Gradle user home (the TestKit directory) for the
 * coverage-enabled worker builds.
 *
 * Reusing one TestKit directory per module lets a single TestKit daemon serve all
 * the test cases, keeping the agent attached with a stable `destfile`.
 */
private const val TESTKIT_HOME_DIR: String = "testkit-home"
