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

package io.spine.tools.gradle.testing

import java.io.File
import org.gradle.testkit.runner.GradleRunner

/**
 * The names of the system properties with which the consuming build enables
 * the coverage of Gradle TestKit worker JVMs.
 *
 * The consuming build sets them on the test JVM, as described in [GradleProject].
 *
 * The property names are part of the contract with consuming builds and must not
 * change: a build sets the properties by name, and is versioned independently of
 * this library.
 */
public object TestKitCoverage {

    // The `io.spine.gradle.testing.enableTestKitCoverage` extension in `config`'s
    // `buildSrc` sets these properties. It repeats the names, because it does not
    // depend on this library.

    /**
     * The prefix shared by the names of the properties.
     */
    private const val PREFIX: String = "io.spine.tools.gradle.testkit.coverage"

    /**
     * The name of the system property carrying the absolute path to the JaCoCo
     * agent JAR to attach to a Gradle TestKit worker JVM. The JAR is the artifact
     * `org.jacoco:org.jacoco.agent:<version>:runtime`.
     */
    public const val AGENT_PROPERTY: String = "$PREFIX.agent"

    /**
     * The name of the system property carrying the absolute path to the directory
     * where the TestKit worker should write its JaCoCo execution data.
     */
    public const val EXEC_DIR_PROPERTY: String = "$PREFIX.execDir"

    /**
     * The name of the system property carrying the absolute path to the TestKit
     * directory shared by the coverage-enabled worker builds of all modules.
     *
     * Optional: when it is absent, each module gets a TestKit directory of its own.
     * With a shared directory, the work the worker builds keep in their Gradle user
     * home, chiefly instrumenting the JARs on the classpath of the plugin under test,
     * is done once rather than once per module, and a CI cache can keep its results
     * between runs. Sharing is safe because the directory holds nothing specific to
     * a module: the agent is passed on the command line, and the workers of each
     * module, started with their own JVM arguments, get daemons of their own.
     *
     * The property takes effect only together with [AGENT_PROPERTY] and [EXEC_DIR_PROPERTY].
     */
    public const val TESTKIT_HOME_PROPERTY: String = "$PREFIX.home"
}

/**
 * Points this runner at the TestKit directory (the Gradle user home of the worker
 * build) used for collecting the coverage of the worker JVM it spawns.
 *
 * Use it together with [withTestKitCoverage]: this function selects the directory,
 * and that one adds the argument that attaches the JaCoCo agent to the worker JVM,
 * so that plugin code executed out-of-process via TestKit is credited to coverage.
 *
 * Does nothing unless the test JVM was started with the
 * [TestKitCoverage.AGENT_PROPERTY] and [TestKitCoverage.EXEC_DIR_PROPERTY] system
 * properties. This keeps the harness a no-op for ordinary runs and for consumers
 * that do not opt in.
 *
 * Supplying those properties on the test JVM is the responsibility of the
 * consuming build. Consumer-facing setup instructions, including a copy-paste
 * Gradle recipe, live in the [GradleProject] KDoc (this function is `internal`,
 * so its own KDoc is not part of the published API documentation).
 *
 * @param preferredTestKitDir The TestKit directory the caller already intends to
 *   use (e.g. the one of [GradleProjectSetup.withSharedTestKitDirectory]), or
 *   `null` to let [coverageTestKitDir] pick one.
 */
internal fun GradleRunner.enableTestKitCoverage(preferredTestKitDir: File? = null) {
    val testKitDir = coverageTestKitDir(preferredTestKitDir) ?: return
    withTestKitDir(testKitDir.apply { mkdirs() })
}

/**
 * Returns the TestKit directory for the coverage-enabled worker builds, or `null`
 * when TestKit coverage is not enabled for this test JVM.
 *
 * In the order of precedence, the directory is:
 *
 *  1. The [preferred] one, which the caller already intends to use.
 *  2. The one [shared by the worker builds of all modules][TestKitCoverage.TESTKIT_HOME_PROPERTY],
 *     when the consuming build names it.
 *  3. A per-module [TESTKIT_HOME_DIR] next to the execution-data directory.
 */
internal fun coverageTestKitDir(preferred: File? = null): File? {
    val coverage = CoverageSetup.fromSystemProperties() ?: return null
    return preferred
        ?: System.getProperty(TestKitCoverage.TESTKIT_HOME_PROPERTY)?.let(::File)
        ?: File(coverage.execDir.parentFile, TESTKIT_HOME_DIR)
}

/**
 * Returns these arguments with the `-Dorg.gradle.jvmargs` argument that attaches
 * the JaCoCo agent to the worker JVM appended, or the same arguments when TestKit
 * coverage is not enabled.
 *
 * The agent appends to a single per-module [execution-data file][EXEC_FILE]; on
 * worker daemon shutdown — which happens after the tests complete and before the
 * coverage report task runs — the data is flushed to disk.
 *
 * The agent is passed on the command line rather than in a `gradle.properties` file
 * in the TestKit directory, so that the directory carries nothing specific to a
 * module and can be shared. The argument replaces the `org.gradle.jvmargs` the
 * worker build would use otherwise, such as the one of the `gradle.properties` of
 * the test project, or one passed earlier in these arguments.
 */
internal fun List<String>.withTestKitCoverage(): List<String> {
    val coverage = CoverageSetup.fromSystemProperties() ?: return this
    val execFile = File(coverage.execDir, EXEC_FILE)
    // Forward slashes work on every platform and need no escaping.
    val jvmArg = "-javaagent:${coverage.agentJar.invariantSeparatorsPath}=" +
            "destfile=${execFile.invariantSeparatorsPath}," +
            "append=true,dumponexit=true,output=file,jmx=false"
    return this + "-Dorg.gradle.jvmargs=$jvmArg"
}

/**
 * The TestKit coverage setup the consuming build passed to this test JVM.
 *
 * @property agentJar The JaCoCo agent JAR.
 * @property execDir The directory for the execution data of the worker JVMs.
 */
private class CoverageSetup(val agentJar: File, val execDir: File) {

    companion object {

        /**
         * Reads the setup from the system properties of this test JVM,
         * or returns `null` when TestKit coverage is not enabled for it.
         */
        fun fromSystemProperties(): CoverageSetup? {
            val agentJar = System.getProperty(TestKitCoverage.AGENT_PROPERTY)
            val execDir = System.getProperty(TestKitCoverage.EXEC_DIR_PROPERTY)
            if (agentJar == null || execDir == null) {
                return null
            }
            return CoverageSetup(File(agentJar), File(execDir))
        }
    }
}

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
 * The name of the directory, placed next to the
 * [execution-data directory][TestKitCoverage.EXEC_DIR_PROPERTY], that serves as the
 * TestKit directory of the coverage-enabled worker builds when no
 * [shared one][TestKitCoverage.TESTKIT_HOME_PROPERTY] is set.
 *
 * Reusing one directory for all the test cases of a module lets a single TestKit
 * daemon serve them.
 */
private const val TESTKIT_HOME_DIR: String = "testkit-home"
