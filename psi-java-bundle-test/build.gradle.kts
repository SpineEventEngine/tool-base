/*
 * Copyright 2024, TeamDev. All rights reserved.
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

plugins {
    module
}

/**
 * This build runs tests from `psi-java` module using the JAR produced by
 * the `psi-java-bundle-jar` module.
 *
 * This is needed to make sure that the FAT JAR produced by the `psi-java-bundle-jar` module
 * satisfies our tests, meaning no important classes or resources were excluded during
 * assembling of the JAR.
 *
 * To achieve the goal, we depend on test classes and the JAR, and then JUnit to
 * run the test classes via the `testClassesDir` property of the `Test` task.
 */
@Suppress("PropertyName")
val ABOUT = ""

val psiJavaProject = project(":psi-java")
val psiJavaBuildDir: DirectoryProperty = psiJavaProject.layout.buildDirectory
val psiTestClasses = files(
    psiJavaBuildDir.dir("classes/kotlin/test"),
    psiJavaBuildDir.dir("resources/test"),
)

val psiBundleJarProject = project(":psi-java-bundle-jar")

dependencies {
    val shadowJar = psiBundleJarProject.tasks.getByName("shadowJar")
    testImplementation(files(shadowJar))
    testImplementation(psiTestClasses)

    testImplementation(project(":plugin-testlib"))
}

tasks.test {
    testClassesDirs = psiTestClasses
    dependsOn(psiJavaProject.tasks.getByName("compileTestKotlin"))
    dependsOn(psiJavaProject.tasks.getByName("processTestResources"))
    dependsOn(psiBundleJarProject.tasks.getByName("jar"))
}
