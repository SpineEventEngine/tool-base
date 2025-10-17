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

package io.spine.tools.protobuf.gradle.plugin

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper
import com.google.protobuf.gradle.GenerateProtoTask
import io.spine.tools.protobuf.gradle.protobufExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * An abstract base for plugins configuring Google Protobuf Gradle plugin.
 */
public abstract class ProtobufSetupPlugin : Plugin<Project> {

    /**
     * Configures all `GenerateProtoTask`s to with the [setup] function.
     */
    @OverridingMethodsMustInvokeSuper
    override fun apply(project: Project): Unit = with(project) {
        pluginManager.withPlugin(ProtobufGradlePlugin.id) {
            protobufExtension?.apply {
                generateProtoTasks.all().configureEach { task ->
                    setup(task)
                }
                afterEvaluate {
                    // "Actualize" the collection of `GenerateProtoTask`s that
                    // may not be brought to life yet because of lazy `configureEach()`
                    // behaviour of new (9.1.0) Gradle.
                    generateProtoTasks.all().size
                }
            }
        }
    }

    /**
     * Overriding classes must configure the given [task].
     */
    protected abstract fun setup(task: GenerateProtoTask)
}
