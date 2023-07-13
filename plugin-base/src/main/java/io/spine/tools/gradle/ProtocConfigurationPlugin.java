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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import com.google.protobuf.gradle.ProtobufExtension;
import com.google.protobuf.gradle.ProtobufExtension.GenerateProtoTaskCollection;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static io.spine.tools.gradle.protobuf.Projects.getProtobufExtension;
import static io.spine.tools.gradle.protobuf.ProtobufDependencies.gradlePlugin;
import static io.spine.tools.gradle.task.Tasks.getDescriptorSetFile;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * An abstract base for Gradle plugins that configure Protobuf compilation.
 *
 * <p>Any extending plugin requires {@code com.google.protobuf} plugin. If it is not applied,
 * no action is performed.
 */
@SuppressWarnings("AbstractClassNeverImplemented")
// Implemented in language-specific parts of Model Compiler.
public abstract class ProtocConfigurationPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager()
               .withPlugin(gradlePlugin.id, plugin -> applyTo(project));
    }

    private void applyTo(Project project) {
        var protobuf = getProtobufExtension(project);
        requireNonNull(protobuf, () ->
                format("Protobuf extension not found in the project `%s`.", project.getName())
        );
        var helper = new Helper(this, project, protobuf);
        helper.configure();
    }

    /**
     * Adds plugins related to the {@code protoc}.
     *
     * @param plugins
     *         container of all plugins
     * @param project
     *         the target project in which the codegen occurs
     * @apiNote overriding methods must invoke super to add the {@code spineProtoc} plugin,
     *         which
     *         is a required plugin
     */
    protected abstract void
    configureProtocPlugins(NamedDomainObjectContainer<ExecutableLocator> plugins, Project project);

    /**
     * Allows subclasses to specify additional generation task settings.
     *
     * @param protocTask
     *         code generation task
     */
    @SuppressWarnings({"NoopMethodInAbstractClass", "unused" /* parameter in no-op impl. */})
    protected void customizeTask(GenerateProtoTask protocTask) {
        // NO-OP by default.
    }

    /**
     * Configures Protobuf Gradle plugin invoking configuration callbacks — such as
     * {@link #configureProtocPlugins(NamedDomainObjectContainer, Project) configureProtocPlugins()}
     * or {@link #customizeTask(GenerateProtoTask) customizeTask()} — provided by the classes
     * derived from {@link ProtocConfigurationPlugin}.
     */
    private static class Helper {

        private final ProtocConfigurationPlugin plugin;
        private final Project project;
        private final ProtobufExtension protobuf;

        private Helper(ProtocConfigurationPlugin plugin,
                       Project project,
                       ProtobufExtension protobuf) {
            this.plugin = plugin;
            this.project = project;
            this.protobuf = protobuf;
        }

        private void configure() {
            configurePlugins();
            protobuf.generateProtoTasks(this::configureProtocTasks);
        }

        private void configurePlugins() {
            protobuf.plugins(plugins -> plugin.configureProtocPlugins(plugins, project));
        }

        /**
         * {@linkplain ProtocConfigurationPlugin#customizeTask Customizes} the Protoc tasks in the
         * given collection.
         *
         * <p>This method copies all the tasks from the given collection into a separate list,
         * iterates over the list, and applies the plugin-implementation-specific customization
         * to each task. The method does the extra copying in order to allow the plugin
         * implementations to add new tasks to the project.
         * Since the {@code GenerateProtoTaskCollection} is a live collection, adding new tasks
         * to the project may cause concurrent modification issues, which are hard to debug.
         *
         * @param tasks Protobuf code generation tasks from {@code protobuf.generateProtoTasks}.
         */
        private void configureProtocTasks(GenerateProtoTaskCollection tasks) {
            var protocTasks = ImmutableList.copyOf(tasks.all());
            protocTasks.forEach(t -> {
                configureDescriptorSetGeneration(t);
                plugin.customizeTask(t);
            });
        }

        private static void configureDescriptorSetGeneration(GenerateProtoTask protocTask) {
            protocTask.setGenerateDescriptorSet(true);
            var options = protocTask.getDescriptorSetOptions();
            var descriptorSetFile = getDescriptorSetFile(protocTask);
            options.setPath(descriptorSetFile.getPath());
            options.setIncludeImports(true);
            options.setIncludeSourceInfo(true);
        }
    }
}
