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

package io.spine.tools.gradle.root

import io.spine.tools.gradle.DslSpec
import io.spine.tools.gradle.applyStandard
import io.spine.tools.gradle.project.ProjectPlugin
import io.spine.tools.gradle.root.RootExtension.Companion.NAME
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.plugins.ExtensionAware

/**
 * Creates [RootExtension] in a project, if it is not already present.
 *
 * The extension is used by Gradle plugins of libraries that extend
 * the [root extension][RootExtension] with custom configuration DSL.
 */
public class RootPlugin :
    ProjectPlugin<RootExtension>(DslSpec(NAME, RootExtension::class)) {

    /**
     * Obtains the [project] to which the plugin is applied.
     */
    override val dslParent: ExtensionAware
        get() = project

    /**
     * Obtains the directory which serves as the root for all the Spine plugins.
     *
     * Conventionally, the path to this directory is `$projectDir/build/spine`.
     */
    public val workingDirectory: Directory by lazy {
        project.rootWorkingDir
    }

    /**
     * Applies the plugin to the given [project] by forcing creation of the [extension].
     *
     * Also applies repositories [standard for Spine SDK][applyStandard].
     */
    override fun apply(project: Project) {
        super.apply(project)
        createExtension()
        project.repositories.applyStandard()
        check(extension != null) {
            "The extension of the `${this::class.simpleName}` has not been created."
        }
    }

    public companion object {

        /**
         * The ID of the plugin.
         */
        public const val ID: String = "io.spine.root"
    }
}
