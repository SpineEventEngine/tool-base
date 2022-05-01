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
import io.spine.io.Resource
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.JarURLConnection
import java.net.URL
import java.util.jar.Attributes
import java.util.jar.Attributes.Name
import java.util.jar.Attributes.Name.IMPLEMENTATION_TITLE
import java.util.jar.Attributes.Name.IMPLEMENTATION_VENDOR
import java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION
import java.util.jar.Manifest
import kotlin.text.Charsets.UTF_8

/**
 * Provides convenience access to standard and custom attributes of a JAR [Manifest]
 * used by the tools of the framework.
 */
public open class KManifest(protected val impl: Manifest) {

    public companion object {

        /**
         * The path to the manifest file in program resources.
         */
        public const val RESOURCE_FILE: String = "META-INF/MANIFEST.MF"

        /**
         * The name of the custom manifest attribute containing a list of dependencies
         * of a software component.
         *
         * @see Dependencies.parse
         */
        @JvmField
        public val DEPENDS_ON_ATTR: Name = Name("Depends-On")

        /**
         * Loads the manifest next to the given class.
         */
        public fun load(cls: Class<*>): KManifest {
            val classFile = cls.simpleName + ".class"
            val classResource = cls.getResource(classFile)!!

            val urlConnection = classResource.openConnection()!!
            if (urlConnection is JarURLConnection) {
                val manifest = urlConnection.manifest
                return KManifest(manifest)
            }

            val classResourcePath = classResource.toString()
            if (!classResourcePath.startsWith("jar")) {
                return loadNonJar(cls, classResourcePath)
            }
            return loadFromJar(classResourcePath)
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

    /**
     * Prints the content of the underlying [Manifest] instance in
     * the form it is stored in a resource file.
     */
    override fun toString(): String {
        val stream = ByteArrayOutputStream()
        stream.use {
            impl.write(it)
        }
        val manifest = String(stream.toByteArray(), UTF_8)
        return manifest
    }
}

private fun loadFromJar(classPath: String): KManifest {
    val manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
            "/" + KManifest.RESOURCE_FILE
    val url = URL(manifestPath)
    val stream = url.openStream()
    return KManifest.load(stream)
}

private fun loadNonJar(cls: Class<*>, classResourcePath: String): KManifest {
    println("**** CLASS RESOURCE PATH: $classResourcePath")
    val manifestResource = Resource.file(KManifest.RESOURCE_FILE, cls.classLoader)
    val allManifests = manifestResource.locateAll()
    println("**** ALL MANIFESTS: $allManifests")
    val urlToCommonPrefix = mutableMapOf<String, URL>()
    allManifests.forEach { url ->
        val commonPrefix = classResourcePath.commonPrefixWith(url.toString())
        urlToCommonPrefix[commonPrefix] = url
    }
    val longest = urlToCommonPrefix.keys.maxByOrNull { it.length }!!
    println("**** LONGEST PREFIX: $longest")
    val nearestManifest = urlToCommonPrefix[longest]!!
    println("**** NEAREST MANIFEST: $nearestManifest")
    val stream = nearestManifest.openStream()
    return KManifest.load(stream)
}

