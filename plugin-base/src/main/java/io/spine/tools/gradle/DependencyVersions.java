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

package io.spine.tools.gradle;

import com.google.common.collect.ImmutableMap;
import io.spine.annotation.Internal;
import io.spine.io.Resource;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * Versions of the dependencies that are used by Spine plugins.
 *
 * @see ProtocConfigurationPlugin
 */
@Internal
public final class DependencyVersions {

    private final ImmutableMap<String, String> versions;

    private DependencyVersions(ImmutableMap<String, String> versions) {
        this.versions = versions;
    }

    /**
     * Loads the versions from a {@code .properties} resource file.
     *
     * <p>The artifactName is the name of the artifact which supplied the {@code .properties} file.
     * The name of the file must be {@code versions-[artifact name].properties},
     * where {@code [artifact name]} is the given {@code artifactName}.
     */
    public static DependencyVersions loadFor(String artifactName) {
        checkNotNull(artifactName);
        String fileName = String.format("/versions-%s.properties", artifactName);
        Resource resource = Resource.file(fileName, DependencyVersions.class.getClassLoader());
        try (Reader reader = resource.openAsText()) {
            Properties properties = new Properties();
            properties.load(reader);
            return loadFrom(properties);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static DependencyVersions loadFrom(Map<?, ?> properties) {
        ImmutableMap<String, String> versions = properties
                .entrySet()
                .stream()
                .collect(toImmutableMap(e -> e.getKey().toString(),
                                        e -> e.getValue().toString()));
        return new DependencyVersions(versions);
    }

    /**
     * Obtains the version of the given artifact by its group ID and name.
     *
     * @return the version or {@code Optional.empty()} if there is no version for the given artifact
     */
    public Optional<String> versionOf(Dependency dependency) {
        String key = dependency.fileSafeId();
        if (versions.containsKey(key)) {
            return Optional.of(versions.get(key));
        } else {
            return Optional.empty();
        }
    }
}
