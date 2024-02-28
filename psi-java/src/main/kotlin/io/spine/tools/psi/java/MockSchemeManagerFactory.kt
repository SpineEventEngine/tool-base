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
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.options.EmptySchemesManager
import com.intellij.openapi.options.Scheme
import com.intellij.openapi.options.SchemeManager
import com.intellij.openapi.options.SchemeManagerFactory
import com.intellij.openapi.options.SchemeProcessor
import java.nio.file.Path

/**
 * A mock implementation of [SchemeManagerFactory] application service used by [Environment].
 *
 * The implementation returns [EmptySchemesManager], which fits our current needs
 * of integration with PSI.
 *
 * The source code of this class was copied from
 * [IntelliJUtils.java](https://github.com/openrewrite/rewrite-python/blob/46c390dcbb33d7b408e679462f38988f34b873fd/src/main/java/org/openrewrite/python/internal/IntelliJUtils.java#L193)
 * of the `rewrite-python` project.
 *
 * @see <a href="https://github.com/openrewrite/rewrite-python/blob/46c390dcbb33d7b408e679462f38988f34b873fd/src/main/java/org/openrewrite/python/internal/IntelliJUtils.java#L193">Original code</a>
 */
internal class MockSchemeManagerFactory : SchemeManagerFactory() {
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
        @Suppress("UNCHECKED_CAST")
        return EmptySchemesManager() as SchemeManager<SCHEME>
    }
}
