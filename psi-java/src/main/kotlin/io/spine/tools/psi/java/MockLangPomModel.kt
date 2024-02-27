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

import com.intellij.openapi.project.Project
import com.intellij.pom.PomModelAspect
import com.intellij.pom.core.impl.PomModelImpl
import com.intellij.pom.event.PomModelEvent
import com.intellij.psi.impl.source.PostprocessReformattingAspect

/**
 * The substitution for the class which is loaded via reflection
 * using nested class name from the resource file
 * `intellij-community/platform/code-style-impl/resources/META-INF/CodeStyle.xml`.
 *
 * Since the class is package-private, we cannot instantiate it for registering
 * the service implementation programmatically, as we do in [Environment].
 * We do not want to use the Java Reflection for this simple class.
 *
 * The original code is [com.intellij.psi.impl.source.PostprocessReformattingAspect.LangPomModel].
 *
 * @see <a href="https://github.com/JetBrains/intellij-community/blob/940f3845a0dbf74bc2f53c339fc09f7956fd5458/platform/code-style-impl/resources/META-INF/CodeStyle.xml#L56">Resource file entry</a>
 */
internal class MockLangPomModel(project: Project) : PomModelImpl(project) {

    private val myAspect = PostprocessReformattingAspect(project)

    override fun <T : PomModelAspect?> getModelAspect(aClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        val result =
            if (myAspect.javaClass == aClass) (myAspect as T)
            else super.getModelAspect(aClass)
        return result
    }

    override fun updateDependentAspects(event: PomModelEvent) {
        super.updateDependentAspects(event)
        myAspect.update(event)
    }

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID: Long = 0L
    }
}
