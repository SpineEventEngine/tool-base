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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import com.google.protobuf.gradle.GenerateProtoTask.DescriptorSetOptions;
import com.google.protobuf.gradle.ProtobufConfigurator;
import com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection;
import io.spine.tools.groovy.ConsumerClosure;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

import static io.spine.tools.gradle.Artifact.PLUGIN_BASE_ID;
import static io.spine.tools.gradle.ProtobufDependencies.gradlePlugin;
import static io.spine.tools.gradle.ProtobufDependencies.protobufCompiler;
import static io.spine.tools.gradle.StandardTypes.getDescriptorSetFile;
import static io.spine.tools.gradle.project.Projects.getGeneratedDir;
import static io.spine.tools.gradle.project.Projects.getProtobufConvention;
import static io.spine.tools.groovy.ConsumerClosure.closure;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;

/**
 * An abstract base for Gradle plugins that configure Protobuf compilation.
 *
 * <p>Any extending plugin requires {@code com.google.protobuf} plugin. If it is not applied,
 * no action is performed.
 */
@SuppressWarnings("AbstractClassNeverImplemented")
// Implemented in language-specific parts of Model Compiler.
public abstract class ProtocConfigurationPlugin implements Plugin<Project> {

    @VisibleForTesting
    static final DependencyVersions versions = DependencyVersions.loadFor(PLUGIN_BASE_ID);

    /**
     * Tells if the source set of the given task contains {@code "test"} in its name.
     *
     * @deprecated Please a name of the source set of the given task instead.
     */
    @SuppressWarnings("WeakerAccess") // This method is used by implementing classes.
    @Deprecated
    protected static boolean isTestsTask(GenerateProtoTask protocTask) {
        return protocTask.getSourceSet()
                         .getName()
                         .contains(TEST_SOURCE_SET_NAME);
    }

    @Override
    public void apply(Project project) {
        project.getPluginManager()
               .withPlugin(gradlePlugin().value(), plugin -> applyTo(project));
    }

    private void applyTo(Project project) {
        getProtobufConvention(project).protobuf(closure(
                       (ProtobufConfigurator protobuf) -> configureProtobuf(project, protobuf)
               ));
    }

    private void configureProtobuf(Project project, ProtobufConfigurator protobuf) {
        Helper helper = new Helper(this, project, protobuf);
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
     * Configures Protobuf Gradle plugin.
     */
    private static class Helper {

        private final ProtocConfigurationPlugin plugin;
        private final Project project;
        private final ProtobufConfigurator protobuf;

        private Helper(ProtocConfigurationPlugin plugin,
                       Project project,
                       ProtobufConfigurator protobuf) {
            this.plugin = plugin;
            this.project = project;
            this.protobuf = protobuf;
        }

        private void configure() {
            setGeneratedFilesBaseDir();
            setProtocArtifact();
            configurePlugins();
            protobuf.generateProtoTasks(closure(this::configureProtocTasks));
        }

        private void setProtocArtifact() {
            ThirdPartyDependency protoc = protobufCompiler();
            String protocArtifact =
                    protoc.withVersionFrom(versions)
                          .notation();
            protobuf.protoc(closure(
                    (ExecutableLocator locator) -> locator.setArtifact(protocArtifact)
            ));
        }

        private void setGeneratedFilesBaseDir() {
            Path generatedFilesBaseDir = getGeneratedDir(project);
            protobuf.setGeneratedFilesBaseDir(generatedFilesBaseDir.toString());
        }

        private void configurePlugins() {
            ConsumerClosure<NamedDomainObjectContainer<ExecutableLocator>> pluginConfig = closure(
                    plugins -> plugin.configureProtocPlugins(plugins, project)
            );
            protobuf.plugins(pluginConfig);
        }

        private void configureProtocTasks(GenerateProtoTaskCollection tasks) {
            // This is a "live" view of the current Gradle tasks.
            Collection<GenerateProtoTask> tasksProxy = tasks.all();

            /*
             *  Creating a hard-copy of "live" view of matching Gradle tasks.
             *
             *  Otherwise, a `ConcurrentModificationException` is thrown upon an attempt to
             *  insert a task into the Gradle lifecycle.
             */
            ImmutableList<GenerateProtoTask> allTasks = ImmutableList.copyOf(tasksProxy);
            for (GenerateProtoTask task : allTasks) {
                configureProtocTask(task);
            }
        }

        private void configureProtocTask(GenerateProtoTask protocTask) {
            configureDescriptorSetGeneration(protocTask);
            plugin.customizeTask(protocTask);
        }

        private void configureDescriptorSetGeneration(GenerateProtoTask protocTask) {
            protocTask.setGenerateDescriptorSet(true);
            DescriptorSetOptions options = protocTask.getDescriptorSetOptions();
            File descriptorSetFile = getDescriptorSetFile(protocTask);
            options.setPath(descriptorSetFile.getPath());
            options.setIncludeImports(true);
            options.setIncludeSourceInfo(true);
        }
    }
}
