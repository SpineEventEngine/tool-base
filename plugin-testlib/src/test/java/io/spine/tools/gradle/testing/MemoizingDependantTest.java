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

package io.spine.tools.gradle.testing;

import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.Dependency;
import io.spine.tools.gradle.ThirdPartyDependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@SuppressWarnings("DuplicateStringLiteralInspection") // Test display names duplication.
@DisplayName("`MemoizingDependant` should")
class MemoizingDependantTest {

    private MemoizingDependant container;

    @BeforeEach
    void setUp() {
        container = new MemoizingDependant();
    }

    @Test
    @DisplayName("memoize a given dependency")
    void addDependency() {
        var dependency = artifact();
        container.implementation(dependency.notation());

        checkDependency(dependency);
    }

    @Test
    @DisplayName("memoize a given exclusion rule")
    void addExclusion() {
        var unwanted = dependency();
        container.exclude(unwanted);

        checkExcluded(unwanted);
    }

    @Nested
    @DisplayName("memoize a forced dependency")
    class MemoizeForcedDependency {

        @Test
        @DisplayName("represented as `Artifact`")
        void representedAsArtifact() {
            var artifact = artifact();
            container.force(artifact);

            checkForced(artifact);
        }

        @Test
        @DisplayName("represented as `String` notation")
        void representedAsString() {
            var artifact = artifact();
            var notation = artifact.notation();
            container.force(notation);

            checkForced(artifact);
        }

        private void checkForced(Artifact artifact) {
            var forcedDependencies = container.forcedDependencies();
            var notation = artifact.notation();
            assertThat(forcedDependencies).contains(notation);
        }
    }

    @Nested
    @DisplayName("remove memoized forced dependency")
    class RemoveMemoizedForcedDependency {

        @Test
        @DisplayName("represented as `Dependency`")
        void representedAsDependency() {
            container.force(artifact());

            var dependency = dependency();
            container.removeForcedDependency(dependency);

            assertThat(container.forcedDependencies()).isEmpty();
        }

        @Test
        @DisplayName("represented as `String` notation")
        void representedAsString() {
            container.force(artifact());

            var notation = artifact().notation();
            container.removeForcedDependency(notation);

            assertThat(container.forcedDependencies()).isEmpty();
        }
    }

    private void checkDependency(Artifact dependency) {
        var dependencies = container.dependencies();
        assertThat(dependencies).contains(dependency.notation());
    }

    private void checkExcluded(Dependency unwanted) {
        var exclusions = container.exclusions();
        assertThat(exclusions).contains(unwanted);
    }

    private static Dependency dependency() {
        return new ThirdPartyDependency("test.dependency", "test-dependency");
    }

    private static String version() {
        return "3.14";
    }

    private static Artifact artifact() {
        return dependency().ofVersion(version());
    }
}
