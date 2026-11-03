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

import io.kotest.matchers.shouldBe
import java.io.File
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS

@DisplayName("Gradle user home lookup should")
internal class GradleUserHomeSpec {

    @Test
    fun `prefer the system property of the Gradle user home`() {
        val home = gradleUserHome(
            systemProperties("gradle.user.home" to "/opt/gradle", "user.home" to "/home/me"),
            environment("GRADLE_USER_HOME" to "/var/gradle")
        )

        home shouldBe File("/opt/gradle")
    }

    @Test
    fun `use the 'GRADLE_USER_HOME' environment variable when the property is not set`() {
        val home = gradleUserHome(
            systemProperties("user.home" to "/home/me"),
            environment("GRADLE_USER_HOME" to "/var/gradle")
        )

        home shouldBe File("/var/gradle")
    }

    @Test
    fun `default to the dot-gradle directory in the home directory of the user`() {
        val home = gradleUserHome(systemProperties("user.home" to "/home/me"), environment())

        home shouldBe File("/home/me", ".gradle")
    }

    @Test
    fun `treat blank values as not set`() {
        val home = gradleUserHome(
            systemProperties("gradle.user.home" to " ", "user.home" to "/home/me"),
            environment("GRADLE_USER_HOME" to "")
        )

        home shouldBe File("/home/me", ".gradle")
    }

    @Test
    fun `put the shared TestKit directory under 'caches' of the Gradle user home`() {
        val dir = defaultSharedTestKitDir(
            systemProperties("user.home" to "/home/me"),
            environment("GRADLE_USER_HOME" to "/var/gradle")
        )

        dir shouldBe File("/var/gradle/caches/spine-testkit")
    }

    @Nested
    @EnabledOnOs(OS.WINDOWS)
    inner class `keep a Windows path` {

        @Test
        fun `given with backslashes`() {
            val fromVariable = defaultSharedTestKitDir(
                systemProperties("user.home" to "C:\\Users\\runneradmin"),
                environment("GRADLE_USER_HOME" to "D:\\a\\.gradle")
            )
            val fromUserHome = defaultSharedTestKitDir(
                systemProperties("user.home" to "C:\\Users\\runneradmin"),
                environment()
            )

            fromVariable.path shouldBe "D:\\a\\.gradle\\caches\\spine-testkit"
            fromUserHome.path shouldBe "C:\\Users\\runneradmin\\.gradle\\caches\\spine-testkit"
        }

        @Test
        fun `given with forward slashes`() {
            val dir = defaultSharedTestKitDir(
                systemProperties("user.home" to "C:\\Users\\runneradmin"),
                environment("GRADLE_USER_HOME" to "D:/a/.gradle")
            )

            dir.path shouldBe "D:\\a\\.gradle\\caches\\spine-testkit"
        }
    }
}

private fun systemProperties(vararg values: Pair<String, String>): (String) -> String? =
    mapOf(*values)::get

private fun environment(vararg values: Pair<String, String>): (String) -> String? =
    mapOf(*values)::get
