/*
 * Copyright 2024, TeamDev. All rights reserved.
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

package io.spine.tools.psi.java

import com.intellij.core.CoreApplicationEnvironment
import com.intellij.mock.MockProject
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.impl.CoreCommandProcessor
import com.intellij.openapi.editor.impl.DocumentWriteAccessGuard
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.extensions.ProjectExtensionPointName
import com.intellij.openapi.extensions.impl.ExtensionsAreaImpl
import com.intellij.pom.PomModel
import com.intellij.pom.core.impl.PomModelImpl
import com.intellij.pom.tree.TreeAspect
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import com.intellij.psi.impl.source.tree.TreeCopyHandler
import io.spine.tools.psi.java.PsiWrite.execute

/**
 * Enhances the PSI [Environment] with the writing abilities.
 *
 * Provides a shortcut for executing a PSI command via the [execute] method.
 */
public object PsiWrite {

    private val lock = Object()
    private var initialized = false

    private val project: MockProject by lazy {
        Environment.project
    }

    private val extensionArea: ExtensionsArea by lazy {
        val result = ExtensionsAreaImpl(project)
        Extensions.setRootArea(result)
        result
    }

    private val commandProcessor: CommandProcessor
    get() {
        if (!initialized) {
            init()
        }
        return CoreCommandProcessor.getInstance()
    }

    public val elementFactory: PsiElementFactory by lazy {
        JavaPsiFacade.getElementFactory(project)
    }

    @JvmStatic
    public fun execute(runnable: Runnable) {
        commandProcessor.executeCommand(project, runnable,
            "ProtoData Annotating Command", "ProtoData Group of Annotation Operations")
    }

    private fun init() {
        if (initialized) {
            return
        }

        Environment.setup()

        // https://github.com/ansman/kotlin/blob/e4b574de3893a2f8bf9c6aa2c975b85f91cae61d/native/native.tests/tests/org/jetbrains/kotlin/konan/test/blackbox/support/group/ExtTestCaseGroupProvider.kt#L872
        synchronized(lock) {
            extensionArea // Force initialization of `ExtensionArea` static.

            registerDynamic(TreeCopyHandler.EP_NAME)
            registerDynamic(PsiTreeChangePreprocessor.EP)
            registerDynamic(DocumentWriteAccessGuard.EP_NAME)

            project.run {
                registerServiceImpl<PomModel>(PomModelImpl::class.java)
                registerService(TreeAspect::class.java)

                registerPoint(PsiTreeChangePreprocessor.EP)
                registerPoint(PsiTreeChangeListener.EP)
            }
        }
        initialized = true
    }
}

private inline fun <reified T : Any> registerDynamic(name: ExtensionPointName<T>) {
    CoreApplicationEnvironment.registerApplicationDynamicExtensionPoint(name.name, T::class.java)
}

private inline fun <reified T : Any> registerDynamic(name: ProjectExtensionPointName<T>) {
    CoreApplicationEnvironment.registerApplicationDynamicExtensionPoint(name.name, T::class.java)
}

private inline fun <reified T : Any> MockProject.registerServiceImpl(impl: Class<out T>) {
    registerService(T::class.java, impl)
}

private inline fun <reified T : Any> MockProject.registerPoint(
    point: ProjectExtensionPointName<T>
) {
    CoreApplicationEnvironment.registerExtensionPoint(extensionArea, point.name, T::class.java)
}
