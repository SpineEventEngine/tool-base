/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.tools.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger

/**
 * Configures the environment for the standalone execution of the IntelliJ IDEA
 * modules associated with PSI.
 *
 * Original code is in `org.jetbrains.kotlin.cli.jvm.compiler.compat.kt`.
 */
public object IdeaStandaloneExecution {

    /**
     * We use the logger from the IntelliJ IDEA codebase in the hope that
     * IDEA would pick up its output log viewer.
     */
    private val LOG: Logger = Logger.getInstance(IdeaStandaloneExecution::class.java)

    // Copy-pasted from com.intellij.openapi.util.BuildNumber#FALLBACK_VERSION
    private const val FALLBACK_IDEA_BUILD_NUMBER = "999.SNAPSHOT"

    private var configured: Boolean = false

    /**
     * Sets up the environment for the standalone execution of the IntelliJ IDEA modules
     * associated with PSI.
     *
     * Call this method before creating other PSI environment objects.
     */
    public fun setUp() {
        synchronized(this) {
            if (!configured) {
                checkInHeadlessMode()
                setSystemProperties()
                configured = true
            }
        }
    }

    private fun checkInHeadlessMode() {
        // If `application` is `null` it means that we are in progress of set-up
        // application environment, i.e., we are not in the running IDEA.
        val application = ApplicationManager.getApplication() ?: return
        if (!application.isHeadlessEnvironment) {
            LOG.error(Throwable(
                "`${this::class.simpleName}` should be called only in headless environment."
            ))
        }
    }

    private fun setSystemProperties() {
        turnHeadlessIfUndefined()

        // As in `org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback()`.
        System.setProperty("idea.io.use.nio2", "true")

        // As in `org.jetbrains.kotlin.cli.jvm.compiler.compat.kt`.
        System.getProperties().let {
            it["project.structure.add.tools.jar.to.new.jdk"] = "false"
            it["psi.track.invalidation"] = "true"
            it["psi.incremental.reparse.depth.limit"] = "1000"
            it["ide.hide.excluded.files"] = "false"
            it["ast.loading.filter"] = "false"
            it["idea.ignore.disabled.plugins"] = "true"
            // Setting the build number explicitly avoids the command-line compiler
            // reading /tmp/build.txt in an attempt to get a build number from there.
            // See intellij platform PluginManagerCore.getBuildNumber.
            it["idea.plugins.compatible.build"] = FALLBACK_IDEA_BUILD_NUMBER
        }
    }

    /**
     * We depend on swing (indirectly through PSI or something), so we want to declare
     * headless mode, to avoid accidentally starting the UI thread.
     *
     * Original code is in `org.jetbrains.kotlin.cli.common.CLITool.doMain()`.
     */
    private fun turnHeadlessIfUndefined() {
        setIfNull("java.awt.headless", "true")
    }

    @Suppress("SameParameterValue")
    private fun setIfNull(propertyName: String, value: String) {
        if (System.getProperty(propertyName) == null) {
            System.setProperty(propertyName, value)
        }
    }
}

