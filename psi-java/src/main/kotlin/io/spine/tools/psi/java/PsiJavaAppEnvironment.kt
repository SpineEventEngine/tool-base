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

import com.intellij.DynamicBundle.LanguageBundleEP
import com.intellij.codeInsight.ContainerProvider
import com.intellij.codeInsight.runner.JavaMainMethodProvider
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.core.JavaCoreApplicationEnvironment
import com.intellij.ide.highlighter.JavaClassFileType
import com.intellij.lang.MetaLanguage
import com.intellij.mock.MockFileDocumentManagerImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.psi.FileContextProvider
import com.intellij.psi.JavaModuleSystem
import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.psi.impl.compiled.ClsCustomNavigationPolicy
import com.intellij.psi.impl.smartPointers.SmartPointerAnchorProvider
import com.intellij.psi.meta.MetaDataContributor
import io.spine.tools.psi.java.IdeaExtensionPoints.registerVersionSpecificAppExtensionPoints

/**
 * An application environment for working with IntelliJ PSI.
 */
public class PsiJavaAppEnvironment private constructor(
    parentDisposable: Disposable
) : JavaCoreApplicationEnvironment(parentDisposable, false) {

    init {
        registerFileType(JavaClassFileType.INSTANCE, "sig")
        replaceFileDocumentManager()
    }

    private fun replaceFileDocumentManager() {
        application.picoContainer.unregisterComponent(FileDocumentManager::class.java.name)
        val documentManager =
            object : MockFileDocumentManagerImpl(null, { chars: CharSequence? ->
                DocumentImpl(
                    chars!!
                )
            }) {
                override fun getCachedDocument(file: VirtualFile): Document? {
                    return super.getDocument(file)
                }
            }

        registerApplicationService(
            FileDocumentManager::class.java,
            documentManager
        )
    }

    override fun createJrtFileSystem(): VirtualFileSystem {
        return JrtFileSystem()
    }

    public companion object {

        /**
         * Creates a new instance of the application environment.
         */
        public fun create(parentDisposable: Disposable): PsiJavaAppEnvironment {
            val environment = PsiJavaAppEnvironment(parentDisposable)
            registerExtensionPoints()
            return environment
        }

        private fun registerExtensionPoints() {
            registerAppExtensionPoints()
            registerVersionSpecificAppExtensionPoints(
                ApplicationManager.getApplication().extensionArea
            )
        }

        private fun registerAppExtensionPoints() {
            @Suppress("UnstableApiUsage")
            register(LanguageBundleEP.EP_NAME)
            register(FileContextProvider.EP_NAME)
            @Suppress("UnstableApiUsage")
            register(MetaDataContributor.EP_NAME)
            register(PsiAugmentProvider.EP_NAME)
            register(JavaMainMethodProvider.EP_NAME)
            register(ContainerProvider.EP_NAME)
            register(MetaLanguage.EP_NAME)
            register(SmartPointerAnchorProvider.EP_NAME)
        }

        private inline fun <reified T: Any> register(name: ExtensionPointName<T>) =
            registerApplicationExtensionPoint(name, T::class.java)
    }
}

private object IdeaExtensionPoints {

    fun registerVersionSpecificAppExtensionPoints(area: ExtensionsArea) = with(area) {
        register(ClsCustomNavigationPolicy.EP_NAME)
        register(JavaModuleSystem.EP_NAME)
    }

    private inline fun <reified T: Any> ExtensionsArea.register(name: ExtensionPointName<T>) =
        CoreApplicationEnvironment.registerExtensionPoint(this, name, T::class.java)
}
