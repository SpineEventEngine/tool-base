/*
 * Copyright 2021, TeamDev. All rights reserved.
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
package io.spine.tools.gradle.task

import io.spine.tools.gradle.SourceSetName
import io.spine.tools.gradle.SourceSetName.Companion.main

/**
 * Names of Gradle tasks defined by the `java` plugin.
 *
 * @see <a href="https://docs.gradle.org/current/userguide/java_plugin.html.sec:java_tasks">
 *     The 'java' plugin documentation</a>
 */
public data class JavaTaskName
internal constructor(private val value: String, val sourceSetName: SourceSetName) : TaskName {

    public companion object {

        /**
         * Obtains a name of the task which compiles Java source files of the specified
         * source set using the JDK compiler.
         */
        @JvmStatic
        public fun compileJava(ssn: SourceSetName): JavaTaskName =
            JavaTaskName("compile${ssn.toInfix()}Java", ssn)

        /**
         * Obtains a name of the task which marks processing of all the classes and resources
         * of the specified source set.
         */
        @JvmStatic
        public fun classes(ssn: SourceSetName): JavaTaskName =
            JavaTaskName(
                "${ssn.toPrefix()}${if (ssn.toPrefix().isEmpty()) "classes" else "Classes"}",
                ssn
            )

        /**
         * Copies resources into the build resources directory of the specified source set.
         */
        @JvmStatic
        public fun processResources(ssn: SourceSetName): JavaTaskName =
            JavaTaskName("process${ssn.toInfix()}Resources", ssn)

        /** Obtains the name of the compilation task for the [main] source set. */
        @JvmField
        public val compileJava: JavaTaskName = compileJava(main)
    }

    /** Obtains the name the task. */
    override fun name(): String = value
}
