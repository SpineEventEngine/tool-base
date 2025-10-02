/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.tools.gradle;

import com.google.common.base.Objects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.proto.FileDescriptors.DESC_EXTENSION;

/**
 * A Maven-style artifact specification.
 *
 * <p>An artifact must have a group, a name, and a version.
 * Also, it may have a classifier and an extension.
 *
 * @deprecated Please use {@code io.spine.tools.meta.MavenArtifact} instead.
 */
@Deprecated
public final class Artifact {

    /**
     * The artifact group used for Spine tools.
     */
    public static final String SPINE_TOOLS_GROUP = "io.spine.tools";

    /**
     * The artifact ID of the published {@code plugin-base} module.
     */
    public static final String PLUGIN_BASE_ID = "plugin-base";

    private static final char COLON = ':';
    private static final char AT = '@';

    private static final char FILE_SAFE_SEPARATOR = '_';

    private final String group;
    private final String name;
    private final String version;
    private final @Nullable String classifier;
    private final @Nullable String extension;

    private Artifact(Builder builder) {
        this.group = checkNotNull(builder.group);
        this.name = checkNotNull(builder.name);
        this.version = checkNotNull(builder.version);
        this.classifier = builder.classifier;
        this.extension = builder.extension;
    }

    /**
     * Creates a new {@code Artifact} from the given
     * {@link org.gradle.api.artifacts.Dependency Dependency}.
     *
     * <p>The passed dependency must have {@code group}, {@code name}, and
     * {@code version} defined.
     *
     * @param d
     *         the Gradle dependency for which to create the artifact
     * @return new instance of {@code Artifact}
     * @throws IllegalArgumentException
     *         if either {@code group}, {@code name}, or {@code version} of the passed
     *         dependency is not defined
     */
    public static Artifact from(Dependency d) {
        var group = ensureProperty(d, d::getGroup, "group");
        var name = ensureProperty(d, d::getName, "name");
        var version = ensureProperty(d, d::getVersion, "version");
        return newBuilder()
                .setGroup(group)
                .setName(name)
                .setVersion(version)
                .build();
    }

    private static <T extends @Nullable Object >
    @NonNull T ensureProperty(Dependency dependency, Supplier<T> accessor, String propertyName) {
        var value = accessor.get();
        checkArgument(
                value != null,
                "The dependency `%s` does not have a property named `%s`.", dependency, propertyName
        );
        return value;
    }

    /**
     * Prints this spec into a single string.
     *
     * <p>The format of the notation is: {@code "group:name:version:classifier@extension"}.
     */
    public String notation() {
        return buildId(COLON, AT);
    }

    /**
     * Prints this spec in the same way as {@link #notation()} but with
     * {@code _} (underscore symbol) instead of any other separator characters.
     */
    public String fileSafeId() {
        return buildId(FILE_SAFE_SEPARATOR, FILE_SAFE_SEPARATOR);
    }

    /**
     * Obtains a descriptor set file of this artifact.
     */
    public File descriptorSetFile() {
        var result = new File(fileSafeId() + DESC_EXTENSION);
        return result;
    }

    private String buildId(char primarySeparator, char secondarySeparator) {
        var result = new StringBuilder(group)
                .append(primarySeparator)
                .append(name)
                .append(primarySeparator)
                .append(version);
        if (classifier != null) {
            result.append(primarySeparator)
                  .append(classifier);
        }
        if (extension != null) {
            result.append(secondarySeparator)
                  .append(extension);
        }
        return result.toString();
    }

    /**
     * Obtains a Maven group of this artifact.
     */
    public String group() {
        return group;
    }

    /**
     * Obtains a Maven name of this artifact.
     */
    public String name() {
        return name;
    }

    /**
     * Obtains a Maven version of this artifact.
     */
    public String version() {
        return version;
    }

    /**
     * Obtains a Maven classifier of this artifact.
     */
    public Optional<String> classifier() {
        return Optional.ofNullable(classifier);
    }

    /**
     * Obtains a Maven extension of this artifact.
     */
    public Optional<String> extension() {
        return Optional.ofNullable(extension);
    }

    @Override
    public String toString() {
        return notation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Artifact artifact)) {
            return false;
        }
        return Objects.equal(group, artifact.group) &&
                Objects.equal(name, artifact.name) &&
                Objects.equal(version, artifact.version) &&
                Objects.equal(classifier, artifact.classifier) &&
                Objects.equal(extension, artifact.extension);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(group, name, version, classifier, extension);
    }

    /**
     * Creates a new builder for the instances of this type.
     * 
     * @return new builder instance
     */
    public static Builder newBuilder() {
        return new Builder();
    }
    
    /**
     * A builder for the {@code Artifact} instances.
     */
    public static final class Builder {

        private @Nullable String group;
        private @Nullable String name;
        private @Nullable String version;
        private @Nullable String classifier;
        private @Nullable String extension;
    
        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        @CanIgnoreReturnValue
        public Builder setGroup(String group) {
            this.group = checkNotNull(group);
            return this;
        }

        public @Nullable String getGroup() {
            return group;
        }

        @CanIgnoreReturnValue
        public Builder useSpineToolsGroup() {
            return setGroup(SPINE_TOOLS_GROUP);
        }

        @CanIgnoreReturnValue
        public Builder setName(String name) {
            this.name = checkNotNull(name);
            return this;
        }

        public @Nullable String getName() {
            return name;
        }

        @CanIgnoreReturnValue
        public Builder setVersion(String version) {
            this.version = checkNotNull(version);
            return this;
        }

        public @Nullable String getVersion() {
            return version;
        }

        @CanIgnoreReturnValue
        public Builder setClassifier(String classifier) {
            this.classifier = classifier;
            return this;
        }

        @CanIgnoreReturnValue
        public Builder useTestClassifier() {
            return setClassifier("test");
        }

        public @Nullable String getClassifier() {
            return classifier;
        }

        @CanIgnoreReturnValue
        public Builder setExtension(String extension) {
            this.extension = extension;
            return this;
        }

        public @Nullable String getExtension() {
            return extension;
        }

        @CanIgnoreReturnValue
        public Builder setDependency(io.spine.tools.gradle.Dependency dependency) {
            checkNotNull(dependency);
            this.group = dependency.groupId();
            this.name = dependency.name();
            return this;
        }

        public io.spine.tools.gradle.Dependency getDependency() {
            checkNotNull(group);
            checkNotNull(name);
            return new ThirdPartyDependency(group, name);
        }

        /**
         * Creates a new instance of {@link Artifact}.
         */
        public Artifact build() {
            checkNotNull(group);
            checkNotNull(name);
            checkNotNull(version);
            return new Artifact(this);
        }
    }
}
