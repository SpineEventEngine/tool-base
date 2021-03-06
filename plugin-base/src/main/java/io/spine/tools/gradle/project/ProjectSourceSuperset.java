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

import io.spine.tools.gradle.GeneratedSourceRoot;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.project.Projects.getSourceSets;

/**
 * A {@link SourceSuperset} implementation based on source sets of a Gradle project.
 *
 * <p>{@code ProjectSourceSuperset} does not try to resolve any files or find the current project
 * source sets unless {@link #register} is called.
 */
public final class ProjectSourceSuperset implements SourceSuperset {

    private final Project project;

    private ProjectSourceSuperset(Project project) {
        this.project = project;
    }

    /**
     * Creates a new instance for the given project.
     */
    public static ProjectSourceSuperset of(Project project) {
        checkNotNull(project);
        return new ProjectSourceSuperset(project);
    }

    @Override
    public void register(GeneratedSourceRoot rootDirectory) {
        checkNotNull(rootDirectory);
        var sourceSets = getSourceSets(project);
        sourceSets.forEach(sourceSet -> {
            var scopeDir = rootDirectory.sourceSet(sourceSet.getName());
            sourceSet.getJava()
                     .srcDirs(scopeDir.java(), scopeDir.spine(), scopeDir.grpc());
            sourceSet.getResources()
                     .srcDir(scopeDir.resources());
        });
    }
}
