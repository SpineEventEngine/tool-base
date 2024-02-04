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
import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import io.spine.io.Closeable
import io.spine.tools.psi.IdeaStandaloneExecution
import io.spine.tools.psi.java.Environment.setup

/**
 * An environment for working with IntelliJ PSI.
 *
 * Before using PSI, please call [setup].
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
     * @throws IllegalStateException if accessed before [setup] or after [close] is called.
     */
    public val project: MockProject
        get() {
            check(_project != null) {
                "PSI environment is not set up." +
                        " Please call `Environment.setup()` before accessing PSI."
            }
            return _project!!
        }

    public fun setup() {
        if (isOpen) {
            return
        }
        synchronized(lock) {
            IdeaStandaloneExecution.setup()
            rootDisposable = Disposer.newDisposable()
            appEnvironment = PsiJavaAppEnvironment.create(rootDisposable!!)
            projectEnvironment = JavaCoreProjectEnvironment(rootDisposable!!, appEnvironment)
            _project = projectEnvironment.project
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
