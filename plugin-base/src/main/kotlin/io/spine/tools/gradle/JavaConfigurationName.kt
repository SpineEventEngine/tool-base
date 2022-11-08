/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.tools.gradle

import io.spine.tools.code.SourceSetBasedName
import io.spine.tools.code.SourceSetName
import io.spine.tools.code.SourceSetName.Companion.main
import io.spine.tools.code.SourceSetName.Companion.test

/**
 * Names of Java project configurations used by the Spine Gradle plugins.
 *
 * @see <a href="https://docs.gradle.org/current/userguide/managing_dependency_configurations.html">Gradle documentation</a>
 */
public class JavaConfigurationName(value: String, sourceSetName: SourceSetName) :
    SourceSetBasedName(value, sourceSetName), ConfigurationName {

    public companion object {

        /**
         * The classpath of a Gradle build process.
         */
        @JvmField
        public val classpath: ConfigurationName = ConfigurationNameImpl()

        private fun prefixed(ssn: SourceSetName, value: String) =
            JavaConfigurationName("${ssn.toPrefix()}${suffix(ssn, value)}", ssn)

        /**
         * Obtains a name of the `annotationProcessor` configuration for the specified source set.
         */
        @JvmStatic
        public fun annotationProcessor(ssn: SourceSetName): ConfigurationName =
            prefixed(ssn, "annotationProcessor")

        /**
         * Obtains a name of the `implementation` configuration for the specified source set.
         */
        @JvmStatic
        public fun implementation(ssn: SourceSetName): ConfigurationName =
            prefixed(ssn, "implementation")

        /**
         * Obtains a name of the `compileClasspath` configuration for the specified source set.
         */
        @JvmStatic
        public fun compileClasspath(ssn: SourceSetName): ConfigurationName =
            prefixed(ssn, "compileClasspath")

        /**
         * Obtains a name of the `runtimeOnly` configuration for the specified source set.
         */
        @JvmStatic
        public fun runtimeOnly(ssn: SourceSetName): ConfigurationName =
            prefixed(ssn, "runtimeOnly")

        /**
         * Obtains a name of the configuration `runtimeClasspath` for the specified source set.
         */
        @JvmStatic
        public fun runtimeClasspath(ssn: SourceSetName): ConfigurationName =
            prefixed(ssn, "runtimeClasspath")

        /**
         * The annotation processors used during the compilation of this module.
         *
         * These dependencies are not accessible to the user at compile-time or at runtime directly.
         */
        @Suppress("unused")
        @JvmField
        public val annotationProcessor: ConfigurationName = annotationProcessor(main)

        /**
         * The API of a Java library.
         *
         * The dependencies are available at compile-time and runtime.
         *
         * Dependencies in this configuration are included as compile-time transitive dependencies
         * in the artifacts of the library.
         */
        @JvmField
        public val api: ConfigurationName = name("api")

        /**
         * Dependencies on which the Java module relies for implementation.
         *
         * The dependencies are available at compile-time and runtime.
         *
         * Dependencies in this configuration are included as runtime transitive dependencies in
         * the artifacts of the module.
         */
        @JvmField
        public val implementation: ConfigurationName = implementation(main)

        /**
         * Dependencies available at compile-time but not at runtime.
         *
         * Suitable for annotations with [java.lang.annotation.RetentionPolicy.CLASS].
         */
        @Suppress("unused")
        @JvmField
        public val compileOnly: ConfigurationName = name("compileOnly")

        /**
         * All the dependencies included for the Java module compilation.
         *
         * Users cannot add dependencies directly to this configuration.
         * However, this configuration may be resolved.
         */
        @JvmField
        public val compileClasspath: ConfigurationName = compileClasspath(main)

        /**
         * Dependencies available at runtime but not at compile-time.
         *
         * Suitable for SPI implementations loaded via [java.util.ServiceLoader] or other
         * classpath scanning utilities.
         */
        @Suppress("unused")
        @JvmField
        public val runtimeOnly: ConfigurationName = runtimeOnly(main)

        /**
         * All the dependencies included for the Java module runtime.
         *
         * Users cannot add dependencies directly to this configuration.
         * However, this configuration may be resolved.
         */
        @JvmField
        public val runtimeClasspath: ConfigurationName = runtimeClasspath(main)

        /**
         * The annotation processors used during the compilation of the tests of this module.
         *
         * These dependencies are not accessible to the user at compile-time or at runtime directly.
         */
        @JvmField
        public val testAnnotationProcessor: ConfigurationName = annotationProcessor(test)

        /**
         * Dependencies on which the Java module tests rely for implementation.
         *
         * The dependencies are available at compile-time of the test code and at the test runtime.
         */
        @JvmField
        public val  testImplementation: ConfigurationName = implementation(test)

        /**
         * All the dependencies included for the Java module tests compilation.
         *
         * Users cannot add dependencies directly to this configuration.
         * However, this configuration may be resolved.
         */
        @Suppress("unused")
        @JvmField
        public val testCompileClasspath: ConfigurationName = compileClasspath(test)

        /**
         * Dependencies available at test runtime but not at compile-time.
         *
         * For example, JUnit runners may be depended on with this configuration.
         */
        @Suppress("unused")
        @JvmField
        public val testRuntimeOnly: ConfigurationName = runtimeOnly(test)

        /**
         * All the dependencies included for the Java module test runtime.
         *
         * Users cannot add dependencies directly to this configuration.
         * However, this configuration may be resolved.
         */
        @JvmField
        public val testRuntimeClasspath: ConfigurationName = runtimeClasspath(test)

        /**
         * Configuration that allows to compile `.proto` files from the dependencies.
         */
        @JvmField
        public val protobuf: ConfigurationName = name("protobuf")

        /**
         * A Spine-specific configuration used to download and resolve artifacts.
         */
        @JvmField
        @io.spine.annotation.Internal
        @Deprecated("No longer used in v2.x.")
        public val fetch: ConfigurationName = name("fetch")

        /**
         * The `compile` configuration.
         *
         * Although the `compile` configuration is deprecated in Gradle, it is still used
         * to define Protobuf dependencies without re-generating the Java/JS sources from
         * the upstream Protobuf definitions.
         *
         * For other cases, consider using alternatives: [implementation] and [api].
         */
        @JvmField
        @Deprecated("Deprecated since Gradle 5.0.")
        public val compile: ConfigurationName = name("compile")

        private fun name(value: String) = ConfigurationNameImpl(value)
    }
}

private data class ConfigurationNameImpl(private val value: String = "classpath") :
    ConfigurationName {
    override fun name(): String = value
}
