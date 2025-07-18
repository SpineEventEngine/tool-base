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

package io.spine.tools.gradle;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A Maven-style dependency specification.
 *
 * <p>Consists of a group ID and a name. Does not specify a concrete artifact.
 *
 * @deprecated Use the {@code io.spine.tools.meta.Dependency} interface instead.
 */
@Deprecated(forRemoval = true)
public interface Dependency {

    /**
     * Obtains the Maven group ID of this dependency.
     */
    String groupId();

    /**
     * Obtains the name of this dependency.
     */
    String name();

    /**
     * Obtains this dependency group ID and name joined on an underscore symbol ({@code _}).
     */
    default String fileSafeId() {
        return groupId() + '_' + name();
    }

    /**
     * Compiles an {@link Artifact} for this dependency with a version from the given
     * set of versions.
     *
     * @deprecated Use the artifact metadata functionality in the `jvm-tools` module instead.
     */
    @Deprecated(forRemoval = true)
    default Artifact withVersionFrom(DependencyVersions versions) {
        checkNotNull(versions);
        var version = versions.versionOf(this)
                              .orElseThrow(() -> newIllegalStateException(
                                      "No version found for `%s`.", this
                              ));
        return ofVersion(version);
    }

    /**
     * Compiles an {@link Artifact} for this dependency.
     *
     * @param version
     *         the version of the artifact, e.g. {@code 1.0.0}
     */
    default Artifact ofVersion(String version) {
        checkNotNull(version);
        return Artifact.newBuilder()
                .setGroup(groupId())
                .setName(name())
                .setVersion(version)
                .build();
    }
}
