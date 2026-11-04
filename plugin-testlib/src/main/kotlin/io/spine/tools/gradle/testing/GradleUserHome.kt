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
 * Returns the TestKit directory that a project set up with
 * [GradleProjectSetup.withSharedTestKitDirectory] uses when given none:
 * `caches/spine-testkit` under the [Gradle user home][gradleUserHome].
 *
 * @param property Returns the value of the system property with the given name.
 * @param environment Returns the value of the environment variable with the given name.
 */
internal fun defaultSharedTestKitDir(
    property: (String) -> String? = System::getProperty,
    environment: (String) -> String? = System::getenv
): File = gradleUserHome(property, environment)
    .resolve(CACHES_DIR)
    .resolve(SHARED_TESTKIT_DIR)

/**
 * Returns the Gradle user home, looked up in the order Gradle uses:
 *
 *  1. The `gradle.user.home` system property.
 *  2. The `GRADLE_USER_HOME` environment variable.
 *  3. The `.gradle` directory in the home directory of the user (`user.home`).
 *
 * A blank value counts as not set. The path is composed with [File], so it gets
 * the separators of the platform, and a Windows path such as `D:\a\.gradle` is
 * kept as given.
 *
 * The lookup runs in the test JVM. That JVM does not see a Gradle user home passed
 * to the outer build only with the `-g` option, so its result is then the default.
 *
 * @param property Returns the value of the system property with the given name.
 * @param environment Returns the value of the environment variable with the given name.
 */
internal fun gradleUserHome(
    property: (String) -> String?,
    environment: (String) -> String?
): File {
    val given = property(GRADLE_USER_HOME_PROPERTY).nonBlank()
        ?: environment(GRADLE_USER_HOME_VARIABLE).nonBlank()
    if (given != null) {
        return File(given)
    }
    val userHome = checkNotNull(property(USER_HOME_PROPERTY)) {
        "The `$USER_HOME_PROPERTY` system property is not set."
    }
    return File(userHome, GRADLE_DIR)
}

private fun String?.nonBlank(): String? = this?.takeIf { it.isNotBlank() }

/**
 * The name of the system property with which Gradle can be given its user home,
 * as by `-Dgradle.user.home=<dir>`.
 */
private const val GRADLE_USER_HOME_PROPERTY: String = "gradle.user.home"

/**
 * The name of the environment variable with which Gradle can be given its user home.
 *
 * It is consulted when the [GRADLE_USER_HOME_PROPERTY] system property is not set.
 */
private const val GRADLE_USER_HOME_VARIABLE: String = "GRADLE_USER_HOME"

/**
 * The name of the standard Java system property carrying the home directory of
 * the user, which holds the [default Gradle user home][GRADLE_DIR].
 */
private const val USER_HOME_PROPERTY: String = "user.home"

/**
 * The name of the directory, in the home directory of the user, that serves as
 * the Gradle user home when neither [GRADLE_USER_HOME_PROPERTY] nor
 * [GRADLE_USER_HOME_VARIABLE] names another one.
 */
private const val GRADLE_DIR: String = ".gradle"

/**
 * The name of the directory under the Gradle user home that holds the caches
 * of Gradle and the [shared TestKit directory][SHARED_TESTKIT_DIR].
 */
private const val CACHES_DIR: String = "caches"

/**
 * The name of the TestKit directory shared by the test builds, placed in
 * the [caches directory][CACHES_DIR] of the Gradle user home.
 */
private const val SHARED_TESTKIT_DIR: String = "spine-testkit"
