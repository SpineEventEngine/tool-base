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

import com.intellij.configurationStore.SchemeNameToFileName
import com.intellij.configurationStore.StreamProvider
import com.intellij.core.JavaCoreProjectEnvironment
import com.intellij.ide.JavaLanguageCodeStyleSettingsProvider
import com.intellij.lang.MetaLanguage
import com.intellij.lang.java.JavaLanguage
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.application.TransactionGuardImpl
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.impl.CoreCommandProcessor
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.extensions.impl.ExtensionsAreaImpl
import com.intellij.openapi.options.EmptySchemesManager
import com.intellij.openapi.options.Scheme
import com.intellij.openapi.options.SchemeManager
import com.intellij.openapi.options.SchemeManagerFactory
import com.intellij.openapi.options.SchemeProcessor
import com.intellij.openapi.util.Disposer
import com.intellij.pom.PomModel
import com.intellij.pom.core.impl.PomModelImpl
import com.intellij.pom.tree.TreeAspect
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiNameHelper
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.psi.codeStyle.AppCodeStyleSettingsManager
import com.intellij.psi.codeStyle.CodeStyleSchemes
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.CodeStyleSettingsService
import com.intellij.psi.codeStyle.CodeStyleSettingsServiceImpl
import com.intellij.psi.codeStyle.FileIndentOptionsProvider
import com.intellij.psi.codeStyle.FileTypeIndentOptionsProvider
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.intellij.psi.codeStyle.ProjectCodeStyleSettingsManager
import com.intellij.psi.codeStyle.ReferenceAdjuster
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.PsiNameHelperImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import com.intellij.psi.impl.source.codeStyle.JavaCodeStyleManagerImpl
import com.intellij.psi.impl.source.codeStyle.JavaReferenceAdjuster
import com.intellij.psi.impl.source.codeStyle.PersistableCodeStyleSchemes
import io.spine.io.Closeable
import io.spine.tools.psi.IdeaStandaloneExecution
import io.spine.tools.psi.java.Environment.setUp
import io.spine.tools.psi.register
import io.spine.tools.psi.registerPoint
import io.spine.tools.psi.registerServiceImpl
import java.nio.file.Path

/**
 * An environment for working with IntelliJ PSI.
 *
 * Before using PSI, please call [setUp].
 */
public object Environment : Closeable {

    private val lock = Object()

    private var _application: MockApplication? = null
    private var _project: MockProject? = null
    private var rootDisposable: Disposable? = null

    private lateinit var appEnvironment: PsiJavaAppEnvironment
    private lateinit var projectEnvironment: JavaCoreProjectEnvironment

    /**
     * Obtains the application initialized in this environment.
     */
    public val application: MockApplication
        get() {
            ensureSetUp()
            check(_application != null) {
                "PSI environment does not have the `application` initialized."
            }
            return _application!!
        }

    /**
     * Obtains the project initialized in this environment.
     */
    public val project: MockProject
        get() {
            ensureSetUp()
            check(_project != null) {
                "PSI environment does not have the `project` initialized."
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

    /**
     * Obtains the instance of [CommandProcessor] ensuring the [Environment] is
     * [initialized][setUp].
     */
    internal val commandProcessor: CommandProcessor
        get() {
            ensureSetUp()
            return CoreCommandProcessor.getInstance()
        }

    private fun ensureSetUp() {
        if (!isOpen) {
            setUp()
        }
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
            IdeaStandaloneExecution.setUp()
            rootDisposable = Disposer.newDisposable()
            appEnvironment = PsiJavaAppEnvironment.create(rootDisposable!!)

            _application = appEnvironment.application
            registerApplicationServices()

            projectEnvironment = JavaCoreProjectEnvironment(rootDisposable!!, appEnvironment)
            _project = projectEnvironment.project

            createRootArea()
            // The below call uses indirectly `Extensions.getRootArea()`.
            // So it must follow the creation of the area.
            PsiJavaAppEnvironment.registerExtensionPoints()
            registerProjectExtensions()
            addOtherExtensions()
        }
    }

    private fun registerApplicationServices() {
        with(_application!!) {
            registerServiceImpl<TransactionGuard>(TransactionGuardImpl::class.java)
            registerServiceImpl<CodeStyleSettingsService>(CodeStyleSettingsServiceImpl::class.java)
            registerServiceImpl<CodeStyleSchemes>(PersistableCodeStyleSchemes::class.java)
            registerServiceImpl<SchemeManagerFactory>(MockSchemeManagerFactory::class.java)
            registerServiceImpl<AppCodeStyleSettingsManager>(AppCodeStyleSettingsManager::class.java)
        }
    }

    private fun registerProjectExtensions() {
        project.run {
            registerServiceImpl<PomModel>(PomModelImpl::class.java)
            registerServiceImpl<PsiNameHelper>(PsiNameHelperImpl::class.java)
            registerServiceImpl<PsiManager>(PsiManagerImpl::class.java)

            registerServiceImpl<JavaCodeStyleManager>(
                JavaCodeStyleManagerImpl::class.java
            )
            registerServiceImpl<CodeStyleSettingsManager>(
                ProjectCodeStyleSettingsManager::class.java
            )
            registerServiceImpl<ProjectCodeStyleSettingsManager>(
                ProjectCodeStyleSettingsManager::class.java
            )

            registerService(TreeAspect::class.java)

            registerPoint(PsiTreeChangePreprocessor.EP)
            registerPoint(PsiTreeChangeListener.EP)
            registerPoint(PsiElementFinder.EP)
        }
    }

    private fun addOtherExtensions() {
        ReferenceAdjuster.Extension.INSTANCE.addExplicitExtension(
            JavaLanguage.INSTANCE,
            JavaReferenceAdjuster()
        )
        LanguageCodeStyleSettingsProvider.registerSettingsPageProvider(
            JavaLanguageCodeStyleSettingsProvider()
        )
    }

    private fun createRootArea() {
        val rootArea = ExtensionsAreaImpl(_project!!)
        Extensions.setRootArea(rootArea)
        registerInArea(rootArea)
    }

    private fun registerInArea(extensionArea: ExtensionsArea) {
        with(extensionArea) {
            register(MetaLanguage.EP_NAME)
            register(PsiAugmentProvider.EP_NAME)
            register(CodeStyleSettingsProvider.EXTENSION_POINT_NAME)
            register(
                LanguageCodeStyleSettingsProvider.EP_NAME,
                JavaLanguageCodeStyleSettingsProvider::class.java
            )
            register(FileIndentOptionsProvider.EP_NAME)
            register(FileTypeIndentOptionsProvider.EP_NAME)
        }
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

private class MockSchemeManagerFactory : SchemeManagerFactory() {
    override fun <SCHEME : Scheme, MUTABLE_SCHEME : SCHEME> create(
        directoryName: String,
        processor: SchemeProcessor<SCHEME, MUTABLE_SCHEME>,
        presentableName: String?,
        roamingType: RoamingType,
        schemeNameToFileName: SchemeNameToFileName,
        streamProvider: StreamProvider?,
        directoryPath: Path?,
        isAutoSave: Boolean
    ): SchemeManager<SCHEME> {
        /**
         * Inspired by https://github.com/openrewrite/rewrite-python/blob/46c390dcbb33d7b408e679462f38988f34b873fd/src/main/java/org/openrewrite/python/internal/IntelliJUtils.java#L193
         */
        @Suppress("UNCHECKED_CAST")
        return EmptySchemesManager() as SchemeManager<SCHEME>
    }
}
