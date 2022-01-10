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

import com.google.common.base.Function;
import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.Dependency;
import io.spine.tools.gradle.ThirdPartyDependency;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.internal.artifacts.DefaultExcludeRule;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.truth.Correspondence.transforming;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.JavaConfigurationName.implementation;
import static io.spine.tools.gradle.JavaConfigurationName.runtimeClasspath;
import static io.spine.tools.gradle.JavaConfigurationName.testRuntimeClasspath;
import static io.spine.tools.gradle.project.Projects.configuration;
import static java.lang.String.format;

@DisplayName("`DependantProject` should")
class DependantProjectTest {

    private Project project;
    private DependantProject dependant;

    @BeforeEach
    void setUp(@TempDir Path projectPath) {
        project = ProjectBuilder.builder()
                .withProjectDir(projectPath.toFile())
                .build();
        project.getPluginManager()
               .apply(JavaPlugin.class);
        dependant = DependantProject.newInstance(project);
    }

    @Test
    @DisplayName("add a given dependency")
    void addDependency() {
        var dependency = artifact();
        dependant.depend(implementation, dependency);

        checkDependency(implementation, dependency);
    }

    @Test
    @DisplayName("add an implementation dependency")
    void implementation() {
        var dependency = artifact();
        dependant.implementation(dependency.notation());

        checkDependency(implementation, dependency);
    }

    @Test
    @DisplayName("exclude dependencies")
    void excludeDependencies() {
        var unwanted = dependency();
        dependant.exclude(unwanted);

        checkExcluded(runtimeClasspath, unwanted);
        checkExcluded(testRuntimeClasspath, unwanted);
    }

    @Nested
    @DisplayName("force the dependency to resolve to a particular version")
    class ForceDependency {

        private Artifact artifact;

        @BeforeEach
        void obtainArtifact() {
            artifact = artifact();
        }

        @Test
        @DisplayName("when the dependency is represented as `Artifact`")
        void asArtifact() {
            dependant.force(artifact);
            checkForced();
        }

        @Test
        @DisplayName("when the dependency is represented as `String` notation")
        void asString() {
            dependant.force(artifact.notation());
            checkForced();
        }

        private void checkForced() {
            var configurations = project.getConfigurations();
            configurations.forEach(this::checkForced);
        }

        private void checkForced(Configuration config) {
            var forcedModules = config.getResolutionStrategy().getForcedModules();
            var description = "in string form equals to";
            var correspondence =
                    transforming(new SelectorToNotation(), description);
            assertThat(forcedModules)
                    .comparingElementsUsing(correspondence)
                    .contains(artifact.notation());
        }
    }

    @Nested
    @DisplayName("remove a dependency from the forced dependencies list")
    class RemoveForcedDependency {

        private Dependency dependency;
        private String notation;

        @BeforeEach
        void forceDependency() {
            dependency = dependency();
            var version = version();
            notation = dependency.ofVersion(version)
                                        .notation();
            project.getConfigurations()
                   .forEach(config -> config.getResolutionStrategy()
                                            .setForcedModules(notation));
        }

        @Test
        @DisplayName("by `Dependency` instance")
        void asDependency() {
            dependant.removeForcedDependency(dependency);

            checkNotForced();
        }

        @Test
        @DisplayName("by `String` notation")
        void asString() {
            dependant.removeForcedDependency(notation);

            checkNotForced();
        }

        private void checkNotForced() {
            project.getConfigurations()
                   .forEach(DependantProjectTest::checkForcedModulesEmpty);
        }
    }

    private void checkDependency(ConfigurationName configuration, Artifact dependency) {
        var dependencies = configuration(project, configuration).getDependencies();
        assertThat(dependencies).hasSize(1);
        var actualDependency = Artifact.from(getOnlyElement(dependencies));
        assertThat(actualDependency).isEqualTo(dependency);
    }

    private void checkExcluded(ConfigurationName fromConfiguration, Dependency unwanted) {
        var runtimeExclusionRules = configuration(project, fromConfiguration).getExcludeRules();
        ExcludeRule excludeRule = new DefaultExcludeRule(unwanted.groupId(), unwanted.name());
        assertThat(runtimeExclusionRules).containsExactly(excludeRule);
    }

    private static void checkForcedModulesEmpty(Configuration config) {
        var forcedModules = config.getResolutionStrategy().getForcedModules();
        assertThat(forcedModules).isEmpty();
    }

    private static Dependency dependency() {
        Dependency dependency = new ThirdPartyDependency("org.example.system", "system-core");
        return dependency;
    }

    private static String version() {
        var version = "1.15.12";
        return version;
    }

    private static Artifact artifact() {
        var artifact = Artifact.newBuilder()
                .useSpineToolsGroup()
                .setName("test-artifact")
                .setVersion("42.0")
                .build();
        return artifact;
    }

    /**
     * Transforms an instance of {@link ModuleVersionSelector} to corresponding
     * Maven artifact notation.
     */
    private static class SelectorToNotation implements Function<ModuleVersionSelector, String> {

        @Override
        public String apply(ModuleVersionSelector selector) {
            var notation = format(
                    "%s:%s:%s", selector.getGroup(), selector.getName(), selector.getVersion()
            );
            return notation;
        }
    }
}
