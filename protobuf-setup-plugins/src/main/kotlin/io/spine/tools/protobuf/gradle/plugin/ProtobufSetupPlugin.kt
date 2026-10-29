/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package io.spine.tools.protobuf.gradle.plugin

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper
import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.ProtobufExtension.GenerateProtoTaskCollection
import io.spine.tools.protobuf.gradle.protobufExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * An abstract base for plugins configuring Google Protobuf Gradle plugin.
 */
public abstract class ProtobufSetupPlugin : Plugin<Project> {

    /**
     * Configures all `GenerateProtoTask`s with the [setup] function.
     */
    @OverridingMethodsMustInvokeSuper
    override fun apply(project: Project): Unit = with(project) {
        pluginManager.withPlugin(ProtobufGradlePlugin.id) {
            protobufExtension?.apply {
                generateProtoTasks.all().configureEach { task ->
                    setup(task)
                }
                // Force the creation of `GenerateProtoTask`s in the first action the Protobuf
                // plugin runs once the project is evaluated. Otherwise, a later action may
                // create the tasks while iterating over the source directories of
                // the source sets, e.g., to add the generated directories to the IDE sources
                // when the `idea` plugin is applied. As `setup()` modifies these source
                // directories, the iteration would fail with `ConcurrentModificationException`.
                generateProtoTasks {
                    it.createAll()
                }
                afterEvaluate {
                    // Create the `GenerateProtoTask`s registered after the actions of
                    // the Protobuf plugin ran, so that `setup()` runs for them too.
                    generateProtoTasks.createAll()
                }
            }
        }
    }

    /**
     * Overriding classes must configure the given [task].
     */
    protected abstract fun setup(task: GenerateProtoTask)
}

/**
 * Creates the registered tasks of this collection, which runs their configuration actions.
 *
 * Querying the size of a task collection creates the tasks registered in it.
 */
private fun GenerateProtoTaskCollection.createAll() {
    all().size
}
