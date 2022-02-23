/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.tools.code.version

import java.util.jar.Attributes.Name
import java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION
import java.util.jar.Manifest


public class KManifest(private val impl: Manifest) {

    public companion object {

        /**
         * The path to the manifest file in program resources.
         */
        private const val MANIFEST_MF = "META-INF/MANIFEST.MF"

        /**
         * The name of the custom manifest attribute containing a list of dependencies.
         */
        public const val DEPENDS_ON_ATTR: String = "Depends-On"

        /**
         * Loads the manifest from the program resources.
         */
        public fun load(cl: ClassLoader): KManifest {
            val stream = cl.getResourceAsStream(MANIFEST_MF)
            stream.use {
                val impl = Manifest(it)
                return KManifest(impl)
            }
        }
    }

    /**
     * Obtains the [`Implementation-Version`][IMPLEMENTATION_VERSION] attribute of the manifest.
     */
    public val implementationVersion: String
        get() {
            val loaded = impl.mainAttributes[IMPLEMENTATION_VERSION]
            require(loaded != null)
            val version = loaded.toString()
            return version
        }

    /**
     * Obtains the dependencies declared in the ['Depends-On'][DEPENDS_ON_ATTR] attribute
     * of the manifest.
     */
    public val dependencies: Dependencies
        get() {
            val depsValue = impl.mainAttributes[Name(DEPENDS_ON_ATTR)].toString()
            val deps = Dependencies.parse(depsValue)
            return deps
        }
}
