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

package io.spine.tools.psi.java

import com.intellij.DynamicBundle.LanguageBundleEP
import com.intellij.codeInsight.ContainerProvider
import com.intellij.codeInsight.runner.JavaMainMethodProvider
import com.intellij.core.CoreApplicationEnvironment.registerExtensionPoint
import com.intellij.core.JavaCoreApplicationEnvironment
import com.intellij.ide.highlighter.JavaClassFileType
import com.intellij.lang.MetaLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.psi.FileContextProvider
import com.intellij.psi.JavaModuleSystem
import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.psi.impl.compiled.ClsCustomNavigationPolicy
import com.intellij.psi.impl.smartPointers.SmartPointerAnchorProvider
import com.intellij.psi.impl.source.tree.TreeCopyHandler
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
            registerPoint(LanguageBundleEP.EP_NAME)
            registerPoint(FileContextProvider.EP_NAME)
            @Suppress("UnstableApiUsage")
            registerPoint(MetaDataContributor.EP_NAME)
            registerPoint(PsiAugmentProvider.EP_NAME)
            registerPoint(JavaMainMethodProvider.EP_NAME)
            registerPoint(ContainerProvider.EP_NAME)
            registerPoint(MetaLanguage.EP_NAME)
            registerPoint(SmartPointerAnchorProvider.EP_NAME)
            registerPoint(TreeCopyHandler.EP_NAME)
        }

        private inline fun <reified T: Any> registerPoint(name: ExtensionPointName<T>) =
            registerApplicationExtensionPoint(name, T::class.java)
    }
}

private object IdeaExtensionPoints {

    fun registerVersionSpecificAppExtensionPoints(area: ExtensionsArea) = with(area) {
        registerPoint(ClsCustomNavigationPolicy.EP_NAME)
        registerPoint(JavaModuleSystem.EP_NAME)
    }

    private inline fun <reified T: Any> ExtensionsArea.registerPoint(name: ExtensionPointName<T>) =
        registerExtensionPoint(this, name, T::class.java)
}
