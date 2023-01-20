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

import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import io.spine.tools.gradle.protobuf.ProtobufGradlePluginAdapter;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static io.spine.tools.gradle.protobuf.ProtobufDependencies.gradlePlugin;
import static io.spine.tools.gradle.protobuf.ProtobufGradlePluginAdapterKt.getProtobufGradlePluginAdapter;
import static io.spine.tools.gradle.task.Tasks.getDescriptorSetFile;

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
        var protobuf = getProtobufGradlePluginAdapter(project);
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
        private final ProtobufGradlePluginAdapter protobuf;

        private Helper(ProtocConfigurationPlugin plugin,
                       Project project,
                       ProtobufGradlePluginAdapter protobuf) {
            this.plugin = plugin;
            this.project = project;
            this.protobuf = protobuf;
        }

        private void configure() {
            configurePlugins();
            protobuf.configureProtoTasks(this::configureProtocTask);
        }

        private void configurePlugins() {
            protobuf.plugins(plugins -> plugin.configureProtocPlugins(plugins, project));
        }

        private void configureProtocTask(GenerateProtoTask protocTask) {
            configureDescriptorSetGeneration(protocTask);
            plugin.customizeTask(protocTask);
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
