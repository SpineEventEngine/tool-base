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

package io.spine.tools.gradle.project;

import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.Dependency;
import io.spine.tools.gradle.JavaConfigurationName;

import static io.spine.tools.gradle.JavaConfigurationName.implementation;

/**
 * Manages the dependencies of a Gradle project.
 *
 * <p>Implementing classes are expected to manipulate with an instance of
 * a {@link org.gradle.api.artifacts.dsl.DependencyHandler DependencyHandler} of the project.
 *
 * @deprecated This interface is going to be removed in future releases.
 */
@Deprecated(forRemoval = true)
public interface Dependant {

    /**
     * Adds a new dependency within a given configuration.
     *
     * @param configuration
     *         the name of the Gradle configuration
     * @param notation
     *         the dependency string, e.g. {@code "io.spine:spine-base:1.0.0"}
     */
    void depend(ConfigurationName configuration, String notation);

    /**
     * Adds a new dependency within a given configuration.
     *
     * @param configuration
     *         the name of the Gradle configuration
     * @param artifact
     *         the artifact on which the configuration is going to depend
     */
    default void depend(ConfigurationName configuration, Artifact artifact) {
        depend(configuration, artifact.notation());
    }

    /**
     * Excludes the given dependency from the project.
     *
     * @param dependency
     *         the dependency to exclude, may refer to multiple artifacts with different versions,
     *         classifiers, etc.
     */
    void exclude(Dependency dependency);

    /**
     * Forces all project configurations to fetch the particular dependency version.
     *
     * @param artifact
     *         the artifact which represents a dependency resolved to the required version
     */
    void force(Artifact artifact);

    /**
     * Forces all project configurations to fetch the particular dependency version.
     *
     * @param artifact
     *         the dependency spec, e.g. {@code com.google.protobuf:protoc:3.9.0}
     */
    void force(String artifact);

    /**
     * Removes a forced dependency from resolution strategies of all project configurations.
     *
     * @param dependency
     *         the dependency to remove from the list of forced dependencies
     */
    void removeForcedDependency(Dependency dependency);

    /**
     * Removes a forced dependency from resolution strategies of all project configurations.
     *
     * @param notation
     *         the dependency spec, e.g. {@code com.google.protobuf:protoc:3.9.0}
     */
    void removeForcedDependency(String notation);

    /**
     * Adds a new dependency within the {@code compile} configuration.
     *
     * @deprecated please use {@link JavaConfigurationName#compileOnly}
     */
    @Deprecated
    default void compile(Artifact artifact) {
        compile(artifact.notation());
    }

    /**
     * Adds a new dependency within the {@code compile} configuration.
     *
     * <p>Though {@code compile} configuration is deprecated in Gradle, it is still used in order to
     * define Protobuf dependencies without re-generating the Java/JS sources from the upstream
     * Protobuf definitions.
     *
     * @deprecated please use {@link JavaConfigurationName#compileOnly}
     */
    @Deprecated
    default void compile(String notation) {
        depend(JavaConfigurationName.compile, notation);
    }

    /**
     * Adds a new dependency within the {@code implementation} configuration.
     *
     * @see #depend(ConfigurationName, String)
     */
    default void implementation(String notation) {
        depend(implementation, notation);
    }
}
