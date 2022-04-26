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

package io.spine.tools.code.manifest

import com.google.common.annotations.VisibleForTesting
import java.io.InputStream
import java.util.jar.Attributes
import java.util.jar.Attributes.Name
import java.util.jar.Attributes.Name.IMPLEMENTATION_TITLE
import java.util.jar.Attributes.Name.IMPLEMENTATION_VENDOR
import java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION
import java.util.jar.Manifest

/**
 * Provides convenience access to standard and custom attributes of a JAR [Manifest]
 * used by the tools of the framework.
 */
public open class KManifest(protected val impl: Manifest) {

    public companion object {

        /**
         * The path to the manifest file in program resources.
         */
        private const val MANIFEST_MF = "META-INF/MANIFEST.MF"

        /**
         * The name of the custom manifest attribute containing a list of dependencies
         * of a software component.
         *
         * @see Dependencies.parse
         */
        @JvmField
        public val DEPENDS_ON_ATTR: Name = Name("Depends-On")

        /**
         * Loads the manifest from the program resources.
         */
        public fun load(cl: ClassLoader): KManifest {
            val stream = cl.getResourceAsStream(MANIFEST_MF)
            check(stream != null) {
                "Unable to load the `$MANIFEST_MF` file from resources."
            }
            return load(stream)
        }

        /**
         * Loads the manifest from the given input stream.
         */
        @VisibleForTesting
        internal fun load(stream: InputStream): KManifest {
            stream.use {
                val impl = Manifest(it)
                return KManifest(impl)
            }
        }
    }

    /**
     * Provides access to [main attributes][Manifest.getMainAttributes] of the manifest.
     */
    protected val mainAttributes: Attributes = impl.mainAttributes

    /**
     * Obtains the [`Implementation-Title`][IMPLEMENTATION_TITLE] attribute of the manifest.
     */
    public val implementationTitle: String? = mainAttributes[IMPLEMENTATION_TITLE]?.toString()

    /**
     * Obtains the [`Implementation-Version`][IMPLEMENTATION_VERSION] attribute of the manifest.
     */
    public val implementationVersion: String? = mainAttributes[IMPLEMENTATION_VERSION]?.toString()

    /**
     * Obtains the [`Implementation-Vendor`][IMPLEMENTATION_VENDOR] attribute of the manifest.
     */
    public val implementationVendor: String? = mainAttributes[IMPLEMENTATION_VENDOR]?.toString()

    /**
     * Obtains the dependencies declared in the ['Depends-On'][DEPENDS_ON_ATTR] attribute
     * of the manifest.
     */
    public val dependencies: Dependencies
        get() {
            val dependsOnAttr = mainAttributes[DEPENDS_ON_ATTR]
            val depsValue = dependsOnAttr.toString()
            val deps = Dependencies.parse(depsValue)
            return deps
        }
}
