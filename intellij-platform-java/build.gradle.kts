/*
 * Copyright 2024, TeamDev. All rights reserved.
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

import io.spine.dependency.lib.IntelliJ

plugins {
    `intellij-platform-jar`
    kotlin("jvm")
}

description = "IntelliJ Platform for Java"

val intellijPlatformModule = project(":intellij-platform")

dependencies {
    api(intellijPlatformModule)

    with(IntelliJ.Platform) {
        listOf(
            codeStyleImpl,
            projectModel,
            projectModelImpl,
            lang,
        ).forEach {
            api(it) {
                // Avoiding duplicated binding with Gradle.
                // Users will need to provide their own dependency on Slf4J.
                exclude(group = "org.slf4j")
            }
        }
    }

    api(IntelliJ.JavaPsi.api)
    api(IntelliJ.JavaPsi.impl)

    // The list of Maven groups we exclude when adding some IntelliJ artifact dependencies.
    val exclusions = listOf(
        // These two are not called as transitive dependencies of IntelliJ API we call.
        "com.jetbrains.infra",
        "ai.grazie.spell",

        // We add required IntelliJ Platform dependencies manually, as they are needed.
        "com.jetbrains.intellij.platform",

        // Avoiding the clash with Gradle dependencies.
        "org.codehaus.groovy",

        // Avoiding the clash with Gradle dependencies.
        "org.slf4j"
    )
    fun ModuleDependency.excludeMany(excl: Iterable<String> = exclusions) {
        excl.forEach { exclude(it) }
    }

    api(IntelliJ.Platform.langImpl) { excludeMany() }

    // To use `AsyncExecutionServiceImpl`, uncomment this:
    api(IntelliJ.Platform.ideImpl) { excludeMany() }

    // To use `NonProjectFileWritingAccessProvider`, uncomment the following:
    api(IntelliJ.Platform.ideCoreImpl) { excludeMany() }

    // To expose `JavaCodeStyleSettings` and other types from `com.intellij.psi.codeStyle`
    // which tools would use for the code style purposes.
    api(IntelliJ.Java.impl) {
        excludeMany(listOf(
            "ai.grazie.nlp",
            "ai.grazie.spell",
            "ai.grazie.utils",
            "org.jetbrains.teamcity",
            "com.jetbrains.infra",

            "com.jetbrains.intellij.platform",

            "com.jetbrains.intellij.jsp",
            "com.jetbrains.intellij.regexp",
            "com.jetbrains.intellij.spellchecker",
            "com.jetbrains.intellij.xml",
            "com.jetbrains.intellij.copyright",

            "com.sun.activation",
            "javax.xml.bind",
            "commons-collections",
            "net.jcip",
            "net.sourceforge.nekohtml",
            "one.util",
            "org.apache.velocity",
            "org.glassfish.jaxb",
            "org.slf4j",
            "oro",
        ))
    }

    //
    // Implementation dependencies on IntelliJ artifacts
    //---------------------------------------------------

    // To access `com.intellij.psi.JspPsiUtil` as a transitive dependency
    // used by `com.intellij.psi.impl.source.codeStyle.ImportHelper`.
    api(IntelliJ.Jsp.jsp) { excludeMany() }

    api(IntelliJ.Xml.xmlPsiImpl) { excludeMany() }

    api(IntelliJ.Platform.analysisImpl) { excludeMany() }
    api(IntelliJ.Platform.indexingImpl) { excludeMany() }
}

/**
 * Exclude files from `intellij-platform` fat JAR when packing fat JAR for this module.
 */
tasks.shadowJar {
    val platformJarTask = intellijPlatformModule.tasks.shadowJar
    dependsOn(platformJarTask)
    val pathsToExclude = mutableListOf<String>()
    doFirst {
        // The path to the file produced for `intellij-platform` module.
        val jarPath = platformJarTask.get().archiveFile.get().asFile
        zipTree(jarPath).visit {
            if (!isDirectory) {
                pathsToExclude.add(this.path)
            }
        }
    }                                                               
    exclude {
        it.path in pathsToExclude
    }
}
