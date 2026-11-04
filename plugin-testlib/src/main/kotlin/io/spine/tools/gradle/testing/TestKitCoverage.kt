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
}

/**
 * Returns these arguments with the `-Dorg.gradle.jvmargs` argument that attaches
 * the JaCoCo agent to the worker JVM appended, or the same arguments when TestKit
 * coverage is not enabled.
 *
 * TestKit coverage is enabled when the test JVM was started with the
 * [TestKitCoverage.AGENT_PROPERTY] and [TestKitCoverage.EXEC_DIR_PROPERTY] system
 * properties. Without them, the harness is a no-op for ordinary runs and for
 * consumers that do not opt in. Supplying the properties is the responsibility of
 * the consuming build; the setup instructions live in the [GradleProject] KDoc.
 *
 * The agent appends to a single per-module [execution-data file][EXEC_FILE]; on
 * worker daemon shutdown — which happens after the tests complete and before the
 * coverage report task runs — the data is flushed to disk.
 *
 * The agent is passed on the command line rather than in a `gradle.properties` file
 * in the TestKit directory, so that coverage does not depend on which TestKit
 * directory the runner uses. The argument replaces the `org.gradle.jvmargs` the
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
