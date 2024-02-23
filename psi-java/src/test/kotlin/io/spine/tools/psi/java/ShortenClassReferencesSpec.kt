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

import com.intellij.codeInsight.actions.OptimizeImportsProcessor
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.formatting.service.CoreFormattingService
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.editor.impl.SelectionModelImpl
import com.intellij.openapi.module.EmptyModuleManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.spine.tools.psi.readResource
import java.io.File
import java.net.URI
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`PsiJavaFile` should support shortening class references")
class ShortenClassReferencesSpec {

    /**
     * This test makes sure that the `IntelliJ.Java.impl` dependency does not break
     * the `GradleProject` API we use in tests.
     */
    @Test
    fun `'GradleProject' guard`(@TempDir projectDir: File) {
        //GradleProject.setupAt(projectDir).create()
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()
        // Add repositories for resolving locally built artifacts (via `mavenLocal()`)
        // and their dependencies via `mavenCentral()`.
        project.repositories.applyStandard()
        project.apply {
            it.plugin("java")
        }
    }

    @Test
    fun `register 'JavaCodeStyleManager' with project`() {
        val project = Environment.project
        val styleManager = JavaCodeStyleManager.getInstance(project)
        styleManager shouldNotBe null
        val fileName = "FieldPath.java"
        val javaFile = readResource(fileName)
        val psiFile = Parser(project = project).parse(javaFile, File(fileName))

        project.addComponent(ModuleManager::class.java, EmptyModuleManager(project))
        // replace with your PsiFile
//        val references = PsiTreeUtil.collectElementsOfType(
//            psiFile,
//            PsiJavaCodeReferenceElement::class.java
//        )
        val codeStyleManager: CodeStyleManager = CodeStyleManager.getInstance(project)

//        Environment.rootArea.register(
//            FormattingService.EP_NAME,
//            CoreFormattingService::class.java
//        )

        @Suppress("DEPRECATION")
        FormattingService.EP_NAME.point.registerExtension(CoreFormattingService())

        FormattingService.EP_NAME.getExtensionList().shouldNotBeEmpty()

//        NonProjectFileWritingAccessProvider.allowWriting(listOf(psiFile.virtualFile))
        execute {
//            references.forEach {
//                styleManager.shortenClassReferences(it)
//            }
            styleManager.shortenClassReferences(psiFile)
            //styleManager.optimizeImports(psiFile)
//            codeStyleManager.reformatText(psiFile, 0, psiFile.textLength)
//            val processor = OptimizeImportsProcessor(project, psiFile)
            val processor = ReformatCodeProcessor(project, psiFile,
                TextRange(0, psiFile.textLength), false)
            processor.runWithoutProgress()
        }

        val text = psiFile.text

        text shouldContain "import com.google.protobuf.GeneratedMessageV3;"
        text shouldNotContain "java.lang"
    }
}

/**
 * Adds the standard Maven repositories to the receiver [RepositoryHandler].
 *
 * This is analogous to the eponymous method in the build scripts with the exception that this
 * method is available at the module's test runtime.
 *
 * Note that not all the Maven repositories may be added to the test projects, but only those that
 * are required for tests. We are not trying to keep these repositories is perfect synchrony with
 * the ones defined in build scripts.
 */
fun RepositoryHandler.applyStandard() {
    mavenLocal()
    mavenCentral()
    val registryBaseUrl = "https://europe-maven.pkg.dev/spine-event-engine"
    maven {
        it.url = URI("$registryBaseUrl/releases")
    }
    maven {
        it.url = URI("$registryBaseUrl/snapshots")
    }
}
