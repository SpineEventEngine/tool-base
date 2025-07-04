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

import com.google.common.collect.ImmutableMap;
import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.Dependency;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.JavaConfigurationName.runtimeClasspath;
import static io.spine.tools.gradle.JavaConfigurationName.testRuntimeClasspath;
import static org.gradle.api.artifacts.ExcludeRule.GROUP_KEY;
import static org.gradle.api.artifacts.ExcludeRule.MODULE_KEY;

/**
 * A {@link Dependant} implemented on top of a {@link DependencyHandler} of a project.
 *
 * @deprecated This class is going to be removed in future releases.
 */
@Deprecated
public final class DependantProject implements Dependant {

    private final DependencyHandler dependencies;
    private final ConfigurationContainer configurations;

    private DependantProject(DependencyHandler dependencies,
                             ConfigurationContainer configurations) {
        this.dependencies = dependencies;
        this.configurations = configurations;
    }

    /**
     * Creates a new instance for the given project.
     *
     * @deprecated This class is going to be removed in future releases.
     */
    @Deprecated
    public static DependantProject from(Project project) {
        return newInstance(project);
    }

    /**
     * Creates a new instance for the given project.
     *
     * @deprecated This class is going to be removed in future releases.
     */
    @Deprecated
    public static DependantProject newInstance(Project project) {
        checkNotNull(project);
        return new DependantProject(project.getDependencies(), project.getConfigurations());
    }

    @Override
    public void depend(ConfigurationName configuration, String notation) {
        dependencies.add(configuration.value(), notation);
    }

    private Configuration configuration(ConfigurationName name) {
        var result = configurations.getByName(name.value());
        return result;
    }

    @Override
    public void exclude(Dependency dependency) {
        var mainConfig = configuration(runtimeClasspath);
        exclude(mainConfig, dependency);

        var testConfig = configuration(testRuntimeClasspath);
        exclude(testConfig, dependency);
    }

    private void forAllConfigurations(Action<Configuration> action) {
        configurations.all(action);
    }

    @Override
    public void force(String artifact) {
        forAllConfigurations(config -> config.getResolutionStrategy()
                                             .force(artifact));
    }

    @Override
    public void force(Artifact artifact) {
        force(artifact.notation());
    }

    @Override
    public void removeForcedDependency(Dependency dependency) {
        forAllConfigurations(config -> removeForcedDependency(config, equalsTo(dependency)));
    }

    @Override
    public void removeForcedDependency(String notation) {
        forAllConfigurations(config -> removeForcedDependency(config, equalsTo(notation)));
    }

    /**
     * Excludes the given dependency from the given configuration.
     *
     * @param configuration
     *         the configuration to exclude from
     * @param dependency
     *         the dependency to exclude
     */
    private static void exclude(Configuration configuration, Dependency dependency) {
        configuration.exclude(ImmutableMap.of(
                GROUP_KEY, dependency.groupId(),
                MODULE_KEY, dependency.name()
        ));
    }

    /**
     * Removes a forced dependency from the resolution strategy of a given configuration.
     *
     * @param configuration
     *         the configuration to remove a forced dependency from
     * @param filter
     *         the dependency filter
     */
    private static void
    removeForcedDependency(Configuration configuration, Predicate<ModuleVersionSelector> filter) {
        var forcedModules =configuration.getResolutionStrategy().getForcedModules();
        Collection<ModuleVersionSelector> newForcedModules = new HashSet<>(forcedModules);
        newForcedModules.removeIf(filter);

        configuration.getResolutionStrategy()
                     .setForcedModules(newForcedModules);
    }

    /**
     * Returns a predicate which tests the equality of the given {@link ModuleVersionSelector} to
     * a {@link Dependency}.
     */
    private static Predicate<ModuleVersionSelector> equalsTo(Dependency dependency) {
        return selector -> {
            var groupEquals = dependency.groupId().equals(selector.getGroup());
            var nameEquals = dependency.name().equals(selector.getName());
            return groupEquals && nameEquals;
        };
    }

    /**
     * Returns a predicate which tests the equality of the given {@link ModuleVersionSelector} to
     * a dependency spec.
     */
    private static Predicate<ModuleVersionSelector> equalsTo(String notation) {
        return selector -> {
            var artifact = Artifact.newBuilder()
                    .setGroup(selector.getGroup())
                    .setName(selector.getName());
            if (selector.getVersion() != null) {
                artifact.setVersion(selector.getVersion());
            }
            var artifactNotation = artifact.build().notation();
            var result = artifactNotation.equals(notation);
            return result;
        };
    }
}
