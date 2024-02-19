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

import com.intellij.core.JavaCoreProjectEnvironment
import com.intellij.lang.MetaLanguage
import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.application.TransactionGuardImpl
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.impl.CoreCommandProcessor
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.extensions.impl.ExtensionsAreaImpl
import com.intellij.openapi.util.Disposer
import com.intellij.pom.PomModel
import com.intellij.pom.core.impl.PomModelImpl
import com.intellij.pom.tree.TreeAspect
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import io.spine.io.Closeable
import io.spine.tools.psi.IdeaStandaloneExecution
import io.spine.tools.psi.java.Environment.setUp
import io.spine.tools.psi.register
import io.spine.tools.psi.registerPoint
import io.spine.tools.psi.registerServiceImpl

/**
 * An environment for working with IntelliJ PSI.
 *
 * Before using PSI, please call [setUp].
 */
public object Environment : Closeable {

    private val lock = Object()

    private var _project: MockProject? = null
    private var rootDisposable: Disposable? = null

    private lateinit var appEnvironment: PsiJavaAppEnvironment
    private lateinit var projectEnvironment: JavaCoreProjectEnvironment

    /**
     * Obtains the project initialized in this environment.
     *
     * @throws IllegalStateException if accessed before [setUp] or after [close] is called.
     */
    public val project: MockProject
        get() {
            if (!isOpen) {
                setUp()
            }
            check(_project != null) {
                "PSI environment is not set up." +
                        " Please call `Environment.setUp()` before accessing PSI."
            }
            return _project!!
        }

    /**
     * Obtains the instance of [PsiElementFactory] to be used for
     * the current [project][Environment.project].
     */
    public val elementFactory: PsiElementFactory by lazy {
        JavaPsiFacade.getElementFactory(project)
    }

    private val commandProcessor: CommandProcessor
        get() {
            if (!isOpen) {
                setUp()
            }
            return CoreCommandProcessor.getInstance()
        }

    /**
     * Executes the given [Runnable] as a PSI modification
     * [command][CommandProcessor.executeCommand].
     */
    @JvmStatic
    public fun execute(runnable: Runnable) {
        commandProcessor.executeCommand(project, runnable, null, null)
    }

    /**
     * Initializes the PSI environment, making it [open][isOpen].
     *
     * The method checks for the [status][isOpen], so repeated calls are allowed.
     * It is also thread-safe.
     */
    public fun setUp() {
        if (isOpen) {
            return
        }
        synchronized(lock) {
            IdeaStandaloneExecution.setup()
            rootDisposable = Disposer.newDisposable()
            appEnvironment = PsiJavaAppEnvironment.create(rootDisposable!!)
            appEnvironment.application
                .registerServiceImpl<TransactionGuard>(TransactionGuardImpl::class.java)
            projectEnvironment = JavaCoreProjectEnvironment(rootDisposable!!, appEnvironment)
            _project = projectEnvironment.project

            createRootArea()
            // The below call uses indirectly `Extensions.getRootArea()`.
            // So it must follow the creation of the area.
            PsiJavaAppEnvironment.registerExtensionPoints()
            registerProjectExtensions()
        }
    }

    private fun registerProjectExtensions() {
        project.run {
            registerServiceImpl<PomModel>(PomModelImpl::class.java)
            registerServiceImpl<PsiManager>(PsiManagerImpl::class.java)

            // registerServiceImpl<JavaCodeStyleManager>(JavaCodeStyleManagerImpl::class.java)

            registerService(TreeAspect::class.java)

            registerPoint(PsiTreeChangePreprocessor.EP)
            registerPoint(PsiTreeChangeListener.EP)
        }
    }

    private fun createRootArea() {
        val rootArea = ExtensionsAreaImpl(_project!!)
        Extensions.setRootArea(rootArea)
        //TODO:2024-02-15:alexander.yevsyukov: register ALL extensions
        // which target `Extensions.getRootArea()`.
        //registerInArea(ApplicationManager.getApplication().extensionArea)
        registerInArea(rootArea)
    }

    private fun registerInArea(extensionArea: ExtensionsArea) {
        extensionArea.register(MetaLanguage.EP_NAME)
        extensionArea.register(PsiAugmentProvider.EP_NAME)
    }


    override val isOpen: Boolean
        get() = rootDisposable != null

    override fun close() {
        if (isOpen) {
            rootDisposable!!.dispose()
            rootDisposable = null
            _project = null
        }
    }
}
