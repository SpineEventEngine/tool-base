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

package io.spine.tools.version

import io.spine.tools.jvm.jar.KManifest
import io.spine.tools.version.Version.Companion.SEPARATOR
import java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION

/**
 * A version of a software component following the semantic versioning specification.
 *
 * @see <a href="https://semver.org/">Semantic Versioning</a>
 */
public data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String? = null,
    val buildMetadata: String? = null
) : Comparable<Version> {

    /**
     * The string representation of this version.
     */
    val value: String
        get() {
            val base = "$major$SEPARATOR$minor$SEPARATOR$patch"
            val withPreRelease =
                if (preRelease != null) "$base$PRE_RELEASE_SEPARATOR$preRelease" 
                else base
            val withBuildMetadata =
                if (buildMetadata != null) "$withPreRelease$BUILD_METADATA_SEPARATOR$buildMetadata" 
                else withPreRelease
            return withBuildMetadata
        }

    /**
     * Returns `true` if the version contains `snapshot` (in any case),
     * `false` otherwise.
     */
    public fun isSnapshot(): Boolean {
        return toString().contains(SNAPSHOT, ignoreCase = true)
    }

    /**
     * Compares this version with the specified version for order
     * according to semantic versioning rules.
     *
     * @return a negative integer, zero, or a positive integer as this version is less than,
     *   equal to, or greater than the specified version.
     */
    @Suppress("ReturnCount")
    override fun compareTo(other: Version): Int {
        // Compare major, minor, patch.
        val majorComparison = major.compareTo(other.major)
        if (majorComparison != 0) return majorComparison

        val minorComparison = minor.compareTo(other.minor)
        if (minorComparison != 0) return minorComparison

        val patchComparison = patch.compareTo(other.patch)
        if (patchComparison != 0) return patchComparison

        // Pre-release versions have lower precedence than the associated normal version.
        if (preRelease == null && other.preRelease != null) return 1
        if (preRelease != null && other.preRelease == null) return -1

        // Compare pre-release identifiers.
        if (preRelease != null && other.preRelease != null) {
            return comparePreRelease(preRelease, other.preRelease)
        }

        // Build metadata does not affect precedence.
        return 0
    }

    public companion object {

        /**
         * The separator between version components (major, minor, patch).
         */
        public const val SEPARATOR: String = "."

        /**
         * The separator between the version and pre-release identifier.
         */
        public const val PRE_RELEASE_SEPARATOR: String = "-"

        /**
         * The separator between the version and build metadata.
         */
        public const val BUILD_METADATA_SEPARATOR: String = "+"

        /**
         * The identifier for snapshot versions.
         */
        public const val SNAPSHOT: String = "SNAPSHOT"

        /**
         * Obtains the version from the [IMPLEMENTATION_VERSION] attribute of
         * the manifest [loaded][KManifest.Companion.load] for the given class.
         */
        public fun fromManifestOf(cls: Class<*>): Version {
            val manifest = KManifest.Companion.load(cls)
            val implVersion = manifest.implementationVersion
            check(implVersion != null) {
                "Unable to obtain the version:" +
                        " no `${IMPLEMENTATION_VERSION}` attribute found in the manifest."
            }
            val version = parse(implVersion)
            return version
        }

        /**
         * Parses a version string into a [Version] object.
         *
         * @param value the version string to parse.
         * @return the parsed [Version] object.
         * @throws IllegalArgumentException if the version string is not valid.
         */
        public fun parse(value: String): Version {

            fun errorMessage(componentName: String, componentValue: String): String = 
                "The `$componentName` component of the version must be a number: `$componentValue`."

            // Split the version string into its components.
            val buildMetadataSplit = value.split(BUILD_METADATA_SEPARATOR, limit = 2)
            val versionWithoutBuildMetadata = buildMetadataSplit[0]
            val buildMetadata = if (buildMetadataSplit.size > 1) buildMetadataSplit[1] else null

            val preReleaseSplit =
                versionWithoutBuildMetadata.split(PRE_RELEASE_SEPARATOR, limit = 2)
            val versionWithoutPreRelease = preReleaseSplit[0]
            val preRelease = if (preReleaseSplit.size > 1) preReleaseSplit[1] else null

            // Parse the version numbers.
            val versionNumbers = versionWithoutPreRelease.split(SEPARATOR)
            check(versionNumbers.size >= 3) { 
                "A version must have at least major, minor, and patch numbers: `$value`." 
            }

            val majorValue = versionNumbers[0]
            val major = majorValue.toIntOrNull()
            check(major != null) { errorMessage("major", majorValue) }

            val minorValue = versionNumbers[1]
            val minor = minorValue.toIntOrNull()
            check(minor != null) { errorMessage("minor", minorValue) }

            val patchValue = versionNumbers[2]
            val patch = patchValue.toIntOrNull()
            check(patch != null) { errorMessage("patch", patchValue) }

            return Version(major, minor, patch, preRelease, buildMetadata)
        }
    }
}

/**
 * Compares two pre-release version strings according to semantic versioning rules.
 *
 * @param first The first pre-release string to compare.
 * @param second The second pre-release string to compare.
 * @return a negative integer, zero, or a positive integer as the first pre-release
 *   is less than, equal to, or greater than the second pre-release.
 */
@Suppress("ReturnCount")
private fun comparePreRelease(first: String, second: String): Int {
    val firstParts = first.split(SEPARATOR)
    val secondParts = second.split(SEPARATOR)

    // Compare each identifier.
    val minLength = minOf(firstParts.size, secondParts.size)
    for (i in 0 until minLength) {
        val firstPart = firstParts[i]
        val secondPart = secondParts[i]

        // Numeric identifiers always have lower precedence than non-numeric identifiers.
        val firstIsNumeric = firstPart.all { it.isDigit() }
        val secondIsNumeric = secondPart.all { it.isDigit() }

        if (firstIsNumeric && secondIsNumeric) {
            // Compare as integers
            val numComparison = firstPart.toInt().compareTo(secondPart.toInt())
            if (numComparison != 0) return numComparison
        } else if (firstIsNumeric) {
            return -1
        } else if (secondIsNumeric) {
            return 1
        } else {
            // Compare lexically.
            val lexicalComparison = firstPart.compareTo(secondPart)
            if (lexicalComparison != 0) return lexicalComparison
        }
    }

    // If all identifiers are equal up to the length of the shorter one,
    // the longer set of identifiers has a higher precedence.
    return firstParts.size.compareTo(secondParts.size)
}
