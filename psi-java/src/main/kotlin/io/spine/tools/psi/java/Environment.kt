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

import com.intellij.codeInsight.ImportFilter
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.core.JavaCoreProjectEnvironment
import com.intellij.formatting.Formatter
import com.intellij.formatting.FormatterImpl
import com.intellij.formatting.service.CoreFormattingService
import com.intellij.formatting.service.FormattingService
import com.intellij.ide.DataManager
import com.intellij.ide.JavaLanguageCodeStyleSettingsProvider
import com.intellij.ide.impl.HeadlessDataManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ide.util.PropertiesComponentImpl
import com.intellij.lang.ImportOptimizer
import com.intellij.lang.LanguageFormattingRestriction
import com.intellij.lang.MetaLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.java.JavaFormattingModelBuilder
import com.intellij.lang.java.JavaImportOptimizer
import com.intellij.lang.java.JavaLanguage
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.AsyncExecutionService
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.application.TransactionGuardImpl
import com.intellij.openapi.application.impl.AsyncExecutionServiceImpl
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.impl.CoreCommandProcessor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.impl.EditorFactoryImpl
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.extensions.impl.ExtensionsAreaImpl
import com.intellij.openapi.module.EmptyModuleManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.SchemeManagerFactory
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.impl.DirectoryIndex
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy
import com.intellij.openapi.roots.impl.DirectoryIndexImpl
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl
import com.intellij.openapi.util.Disposer
import com.intellij.pom.PomModel
import com.intellij.pom.tree.TreeAspect
import com.intellij.psi.JavaModuleSystem
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiNameHelper
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.psi.codeStyle.AppCodeStyleSettingsManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSchemes
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.CodeStyleSettingsService
import com.intellij.psi.codeStyle.CodeStyleSettingsServiceImpl
import com.intellij.psi.codeStyle.ExternalFormatProcessor
import com.intellij.psi.codeStyle.FileIndentOptionsProvider
import com.intellij.psi.codeStyle.FileTypeIndentOptionsProvider
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.intellij.psi.codeStyle.ProjectCodeStyleSettingsManager
import com.intellij.psi.codeStyle.ReferenceAdjuster
import com.intellij.psi.impl.JavaClassSupersImpl
import com.intellij.psi.impl.JavaPlatformModuleSystem
import com.intellij.psi.impl.JavaPsiImplementationHelper
import com.intellij.psi.impl.JavaPsiImplementationHelperImpl
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.PsiNameHelperImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import com.intellij.psi.impl.search.PsiSearchHelperImpl
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl
import com.intellij.psi.impl.source.codeStyle.JavaCodeStyleManagerImpl
import com.intellij.psi.impl.source.codeStyle.JavaReferenceAdjuster
import com.intellij.psi.impl.source.codeStyle.PersistableCodeStyleSchemes
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.impl.source.javadoc.JavadocManagerImpl
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageManagerImpl
import com.intellij.psi.javadoc.CustomJavadocTagProvider
import com.intellij.psi.javadoc.JavadocManager
import com.intellij.psi.javadoc.JavadocTagInfo
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.JavaClassSupers
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.psi.util.PsiEditorUtilBase
import com.intellij.util.KeyedLazyInstance
import io.spine.io.Closeable
import io.spine.tools.psi.IdeaStandaloneExecution
import io.spine.tools.psi.java.Environment.setUp
import io.spine.tools.psi.register
import io.spine.tools.psi.registerPoint
import io.spine.tools.psi.registerServiceImpl
import io.spine.tools.psi.replaceServiceImpl
import org.jetbrains.annotations.VisibleForTesting

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

    @VisibleForTesting
    public lateinit var rootArea: ExtensionsAreaImpl

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
            registerServiceImpl<TransactionGuard>(TransactionGuardImpl::class)
            registerServiceImpl<CodeStyleSettingsService>(CodeStyleSettingsServiceImpl::class)
            registerServiceImpl<CodeStyleSchemes>(PersistableCodeStyleSchemes::class)
            registerServiceImpl<SchemeManagerFactory>(MockSchemeManagerFactory::class)
            registerServiceImpl<AppCodeStyleSettingsManager>(AppCodeStyleSettingsManager::class)
            registerServiceImpl<AsyncExecutionService>(AsyncExecutionServiceImpl::class)
            registerServiceImpl<PropertiesComponent>(PropertiesComponentImpl::class)
            registerServiceImpl<EditorFactory>(EditorFactoryImpl::class)
            registerServiceImpl<PsiEditorUtil>(PsiEditorUtilBase::class)
            registerServiceImpl<DataManager>(HeadlessDataManager::class)
            registerServiceImpl<Formatter>(FormatterImpl::class)
            registerServiceImpl<JavaClassSupers>(JavaClassSupersImpl::class)
        }
    }

    private fun registerProjectExtensions() {
        project.run {
            replaceServiceImpl<InjectedLanguageManager>(InjectedLanguageManagerImpl::class.java)
            replaceServiceImpl<JavaPsiFacade>(MockJavaPsiFacade::class.java)
            replaceServiceImpl<JavaPsiImplementationHelper>(
                JavaPsiImplementationHelperImpl::class.java
            )

            registerServiceImpl<PomModel>(MockLangPomModel::class.java)

            registerServiceImpl<PsiNameHelper>(PsiNameHelperImpl::class.java)
            registerServiceImpl<PsiManager>(PsiManagerImpl::class.java)

            registerServiceImpl<CodeStyleManager>(CodeStyleManagerImpl::class.java)

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
            registerService(PostprocessReformattingAspect::class.java)
            registerService(ProjectRootManager::class.java, ProjectRootManagerImpl::class.java)
            registerService(ProjectFileIndex::class.java, MockProjectFileIndex::class.java)
            registerService(DirectoryIndex::class.java, DirectoryIndexImpl::class.java)
            registerService(JavadocManager::class.java, JavadocManagerImpl::class.java)
            registerService(PsiSearchHelper::class.java, PsiSearchHelperImpl::class.java)

            registerPoint(PsiTreeChangePreprocessor.EP)
            registerPoint(PsiTreeChangeListener.EP)
            registerPoint(PsiElementFinder.EP)

            CoreApplicationEnvironment.registerExtensionPoint(
                extensionArea,
                DirectoryIndexExcludePolicy.EP_NAME.toString(),
                DirectoryIndexExcludePolicy::class.java
            )

            CoreApplicationEnvironment.registerExtensionPoint(
                extensionArea,
                JavadocTagInfo.EP_NAME.toString(),
                JavadocTagInfo::class.java
            )

            registerPoint(MultiHostInjector.MULTIHOST_INJECTOR_EP_NAME)

            addComponent(ModuleManager::class.java, EmptyModuleManager(project))
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
        @Suppress("DEPRECATION")
        FormattingService.EP_NAME.point.registerExtension(CoreFormattingService())
    }

    private fun createRootArea() {
        rootArea = ExtensionsAreaImpl(_project!!)
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
            register(JavaModuleSystem.EP_NAME, JavaPlatformModuleSystem::class.java)
            register(LanguageFormattingRestriction.EP_NAME)
            register(ExternalFormatProcessor.EP_NAME)
            register(
                FormattingService.EP_NAME,
                CoreFormattingService::class.java
            )

            val importOptimizerEp : ExtensionPointName<KeyedLazyInstance<ImportOptimizer>> =
                ExtensionPointName.create("com.intellij.lang.importOptimizer")
            register(importOptimizerEp)
            @Suppress("DEPRECATION")
            importOptimizerEp.point.registerExtension(
                object : KeyedLazyInstance<ImportOptimizer> {
                    override fun getKey(): String {
                        return JavaLanguage.INSTANCE.id
                    }

                    override fun getInstance(): ImportOptimizer {
                        return JavaImportOptimizer()
                    }
                }
            )

            register(AdditionalLibraryRootsProvider.EP_NAME)
            register(DirectoryIndexExcludePolicy.EP_NAME)
            register(CustomJavadocTagProvider.EP_NAME)
            register(PreFormatProcessor.EP_NAME)
            register(PostFormatProcessor.EP_NAME)

            // From a private const `UnresolvedReferenceQuickFixProvider.EXTENSION_NAME`.
            val unresolvedRefQuickFixEp =
                ExtensionPointName.create<UnresolvedReferenceQuickFixProvider<out PsiReference>>(
                    "com.intellij.codeInsight.unresolvedReferenceQuickFixProvider")
            register(unresolvedRefQuickFixEp)


            val langFormatterEp : ExtensionPointName<KeyedLazyInstance<JavaFormattingModelBuilder>> =
                ExtensionPointName.create("com.intellij.lang.formatter")
            register(langFormatterEp)
            @Suppress("DEPRECATION")
            langFormatterEp.point.registerExtension(
                object : KeyedLazyInstance<JavaFormattingModelBuilder> {
                    override fun getKey(): String {
                        return JavaLanguage.INSTANCE.id
                    }

                    override fun getInstance(): JavaFormattingModelBuilder {
                        return JavaFormattingModelBuilder()
                    }
                }
            )

            register(LanguageInjector.EXTENSION_POINT_NAME)
            register(ImportFilter.EP_NAME)
            register(ReferencesSearch.EP_NAME)
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
