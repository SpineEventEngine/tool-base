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

package io.spine.tools.protobuf.gradle.plugin;

import io.spine.code.proto.FileSet;
import io.spine.tools.code.SourceSetName;
import io.spine.tools.gradle.JavaConfigurationName;
import io.spine.tools.type.FileDescriptorSuperset;
import io.spine.tools.type.MoreKnownTypes;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.project.Projects.configuration;
import static io.spine.tools.gradle.protobuf.Projects.descriptorSetFile;

/**
 * Utilities for Gradle plugins performing code-generation based on {@code .proto} files.
 *
 * @implNote This class uses {@code Supplier}s instead of direct values because at the time
 *         of creation Gradle project is not fully evaluated, and the values are not yet defined.
 */
public final class ProtoFiles {

    /** Prevents instantiation of this utility class. */
    private ProtoFiles() {
    }

    /**
     * Obtains a supplier of file set containing proto files of the specified source set
     * and all proto files from the dependencies.
     *
     * <p>Extends {@linkplain MoreKnownTypes known types} with types form collected files.
     */
    public static Supplier<FileSet> collect(Project project, SourceSetName ssn) {
        checkNotNull(project);
        checkNotNull(ssn);
        Supplier<File> descriptorSetFile = () -> descriptorSetFile(project, ssn);
        var cn = JavaConfigurationName.runtimeClasspath(ssn);
        var configuration = configuration(project, cn);
        return collect(descriptorSetFile, configuration);
    }

    /**
     * Obtains a supplier of all {@code '.proto'} files from the specified descriptor set file and
     * {@code '.proto'} proto from the dependencies of the given configuration.
     *
     * <p>Extends {@linkplain MoreKnownTypes known types} with types form collected files.
     *
     * @param descriptorSetFile
     *         the path to the descriptor set file
     * @param configuration
     *         the configuration to scan descriptor set files from
     */
    private static Supplier<FileSet> collect(Supplier<File> descriptorSetFile,
                                             Configuration configuration) {
        return () -> {
            var superset = new FileDescriptorSuperset();
            configuration.forEach(superset::addFromDependency);
            var suppliedDescriptorSet = descriptorSetFile.get();
            if (suppliedDescriptorSet.exists()) {
                superset.addFromDependency(suppliedDescriptorSet);
            }
            var mergedSet = superset.merge();
            mergedSet.loadIntoKnownTypes();
            return mergedSet.fileSet();
        };
    }
}
