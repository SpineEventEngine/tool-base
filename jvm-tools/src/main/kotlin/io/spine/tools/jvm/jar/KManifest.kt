/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.jvm.jar

import io.spine.tools.jvm.resource.Resource
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
public class KManifest(public val impl: Manifest) {

    public companion object {

        /**
         * The path to the manifest file in program resources.
         */
        public const val RESOURCE_FILE: String = "META-INF/MANIFEST.MF"

        /**
         * The name of a commonly used manifest attribute,
         * describing the contents of the respective JAR file.
         *
         * This attribute name is hard-coded in [Attributes] as a plain string literal,
         * therefore, we re-declare it here as a constant.
         */
        public val BUNDLE_DESCRIPTION: Name = Name("Bundle-Description")

        /**
         * Loads the manifest next to the given class.
         */
        public fun load(cls: Class<*>): KManifest {
            val classResource = cls.toResourceUrl()
            val urlConnection = classResource.openConnection()!!
            if (urlConnection is JarURLConnection) {
                val manifest = urlConnection.manifest
                return KManifest(manifest)
            }
            if (!classResource.isInJar()) {
                return loadNonJar(cls)
            }
            throw IllegalStateException("Unable to load manifest file for" +
                    " the class with the URL `$classResource`.")
        }

        /**
         * Loads the manifest from the given input stream.
         */
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
    private val mainAttributes: Attributes = impl.mainAttributes

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
     * Obtains the [`Bundle-Description`][BUNDLE_DESCRIPTION] attribute of the manifest.
     */
    public val bundleDescription: String? = mainAttributes[BUNDLE_DESCRIPTION]?.toString()

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

/**
 * Obtains URL for obtaining this class as a resource.
 */
private fun Class<*>.toResourceUrl(): URL {
    val classFile = "$simpleName.class"
    val classResource = getResource(classFile)!!
    return classResource
}

/**
 * Tells if this `URL` refers to a resource inside a JAR archive.
 */
private fun URL.isInJar() = toString().startsWith("jar")

/**
 * Loads a manifest "closest" to the given class.
 *
 * The location of the manifest file is obtained as one which has the most lengthy
 * common prefix with the URL of the class.
 *
 * Please see [this Stack Overflow answer](https://stackoverflow.com/a/1273432/2395775)
 * for details. Even though the answer covers the `"jar:"` case of a protocol, this function
 * is meant to handle the URLs with the `"file:"` protocol (primarily used when we test).
 * This is so because when we load a manifest from a JAR, we do it via
 * [JarURLConnection.getManifest].
 *
 * @see KManifest.load
 */
private fun loadNonJar(cls: Class<*>): KManifest {
    val allManifests = manifestsVisibleTo(cls)
    val classResourcePath = cls.toResourceUrl().toString()
    val urlToCommonPrefix = mutableMapOf<String, URL>()
    allManifests.forEach { url ->
        val commonPrefix = classResourcePath.commonPrefixWith(url.toString())
        urlToCommonPrefix[commonPrefix] = url
    }
    val longest = urlToCommonPrefix.keys.maxByOrNull { it.length }!!
    val nearestManifest = urlToCommonPrefix[longest]!!
    val stream = nearestManifest.openStream()
    return KManifest.load(stream)
}

/**
 * Obtains the list of all manifests visible to the given class.
 */
public fun manifestsVisibleTo(cls: Class<*>): List<URL> {
    val manifestResource = Resource.file(KManifest.RESOURCE_FILE, cls.classLoader)
    val result = manifestResource.locateAll()
    return result
}

