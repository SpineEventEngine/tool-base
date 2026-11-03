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

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.spine.tools.gradle.testing.TestKitCoverage.AGENT_PROPERTY
import io.spine.tools.gradle.testing.TestKitCoverage.EXEC_DIR_PROPERTY
import io.spine.tools.gradle.testing.TestKitCoverage.TESTKIT_HOME_PROPERTY
import java.io.File
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("TestKit coverage harness should")
internal class TestKitCoverageSpec {

    private val savedProperties = mutableMapOf<String, String?>()

    @BeforeEach
    fun clearProperties() {
        COVERAGE_PROPERTIES.forEach { name ->
            savedProperties[name] = System.getProperty(name)
            System.clearProperty(name)
        }
    }

    @AfterEach
    fun restoreProperties() {
        savedProperties.forEach { (name, value) ->
            if (value == null) {
                System.clearProperty(name)
            } else {
                System.setProperty(name, value)
            }
        }
    }

    @Test
    fun `leave the arguments intact when coverage is not enabled`() {
        val args = listOf("build")

        args.withTestKitCoverage() shouldBe args
        coverageTestKitDir().shouldBeNull()
    }

    @Test
    fun `stay disabled when only the agent property is set`(@TempDir dir: File) {
        System.setProperty(AGENT_PROPERTY, dir.resolve("agent.jar").path)
        val args = listOf("build")

        args.withTestKitCoverage() shouldBe args
        coverageTestKitDir().shouldBeNull()
    }

    @Nested
    inner class `when coverage is enabled` {

        @TempDir
        lateinit var dir: File

        private val agentJar by lazy { dir.resolve("jacoco/agent.jar") }
        private val execDir by lazy { dir.resolve("build/jacoco-testkit") }

        @BeforeEach
        fun enableCoverage() {
            System.setProperty(AGENT_PROPERTY, agentJar.path)
            System.setProperty(EXEC_DIR_PROPERTY, execDir.path)
        }

        @Test
        fun `append the argument that attaches the agent to the worker JVM`() {
            val args = listOf("build", "--stacktrace").withTestKitCoverage()

            val execFile = execDir.resolve("testkit.exec")
            args shouldBe listOf(
                "build",
                "--stacktrace",
                "-Dorg.gradle.jvmargs=-javaagent:${agentJar.invariantSeparatorsPath}=" +
                        "destfile=${execFile.invariantSeparatorsPath}," +
                        "append=true,dumponexit=true,output=file,jmx=false"
            )
        }

        @Test
        fun `use a TestKit directory per module by default`() {
            coverageTestKitDir() shouldBe dir.resolve("build/testkit-home")
        }

        @Test
        fun `use the shared TestKit directory when the build names one`() {
            val shared = dir.resolve("gradle-home/caches/spine-testkit")
            System.setProperty(TESTKIT_HOME_PROPERTY, shared.path)

            coverageTestKitDir() shouldBe shared
        }

        @Test
        fun `prefer the TestKit directory chosen by the caller`() {
            System.setProperty(TESTKIT_HOME_PROPERTY, dir.resolve("shared").path)
            val preferred = dir.resolve("preferred")

            coverageTestKitDir(preferred) shouldBe preferred
        }
    }
}

private val COVERAGE_PROPERTIES = listOf(AGENT_PROPERTY, EXEC_DIR_PROPERTY, TESTKIT_HOME_PROPERTY)
