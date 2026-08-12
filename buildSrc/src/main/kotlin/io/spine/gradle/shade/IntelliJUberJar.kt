/*
 * Copyright 2026, TeamDev. All rights reserved.
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

package io.spine.gradle.shade

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.publish.maven.MavenPublication

/**
 * The shading policy of the IntelliJ Platform uber-JAR modules
 * (`intellij-platform` and `intellij-platform-java`).
 *
 * Only JetBrains-produced artifacts are welded into the uber JARs.
 * Third-party components published on Maven Central are not shaded;
 * they become `runtime` dependencies of the published POM instead, so that
 * Gradle conflict resolution in consumer projects works on them normally,
 * and the bundled copies can no longer shadow genuine artifacts on
 * a consumer's classpath.
 *
 * JetBrains forks that claim the package names of libraries published on
 * Maven Central are [relocated][relocations]. They exist only in JetBrains
 * repositories, so declaring them in the POM would force every consumer to
 * add those repositories; relocation removes the collision instead.
 *
 * The two uber JARs form a layered pair: `intellij-platform-java` excludes
 * every path already present in the `intellij-platform` JAR. Both modules
 * must therefore apply this policy identically, and the subtraction must
 * account for [relocations] via [sourceFormOf].
 */
object IntelliJUberJar {

    /**
     * Maven groups whose artifacts are welded into the uber JARs.
     *
     * These are the IntelliJ Platform itself (`com.jetbrains.intellij.*`),
     * JetBrains forks of third-party libraries published only in JetBrains
     * repositories (`org.jetbrains.intellij.deps*`), and auxiliary JetBrains
     * artifacts absent from Maven Central (`org.jetbrains.intellij`,
     * e.g. `blockmap`).
     */
    private val shadedGroups = listOf(
        Regex("""com\.jetbrains\.intellij(\..+)?"""),
        Regex("""org\.jetbrains\.intellij(\..+)?"""),
    )

    /**
     * The parent package for the relocated classes of JetBrains forks.
     */
    private const val RELOCATION_PREFIX = "io.spine.tools.ij"

    /**
     * Packages of JetBrains forks that claim the names of libraries published
     * on Maven Central, mapped to the relocated names.
     *
     * Relocation stops the bundled fork classes from clashing with genuine
     * artifacts on a consumer's classpath. Forks with self-namespaced packages,
     * such as `asm-all` (`org.jetbrains.org.objectweb.asm`) or `jb-jdi`
     * (`com.jetbrains.jdi`), need no relocation.
     *
     * The JNA fork is deliberately absent: its native `libjnidispatch`
     * binaries bind JNI entry points to the literal `com.sun.jna` class names,
     * so relocation would break it. It is excluded from the shade by path
     * in `uber-jar-module.gradle.kts` instead. The `jcef` fork stays
     * unrelocated for the same reason: its `org.cef` classes are bound
     * to CEF native libraries.
     */
    private val relocations = mapOf(
        "org.jdom" to "$RELOCATION_PREFIX.jdom",
        "org.apache.log4j" to "$RELOCATION_PREFIX.log4j",
        "gnu.trove" to "$RELOCATION_PREFIX.trove",
        "it.unimi.dsi" to "$RELOCATION_PREFIX.unimi.dsi",
        "org.apache.batik" to "$RELOCATION_PREFIX.batik",
        "com.amazon.ion" to "$RELOCATION_PREFIX.ion",
        "org.apache.commons.imaging" to "$RELOCATION_PREFIX.imaging",
    )

    /**
     * Maven groups deliberately absent from both the shade and the POM.
     *
     * The Kotlin runtime is provided by consumers, as it always was for these
     * artifacts. The rest is machinery never exercised by the headless PSI
     * code: OS integration — terminal emulation and Windows process
     * management — and the BouncyCastle cryptography stack, reached only from
     * the remote-development and plugin-signing paths of `ide-impl`.
     * Tool users who need these components add the genuine Central artifacts
     * explicitly; the platform classes reference them by their original names.
     *
     * Declaring BouncyCastle would publish a self-contradicting graph, since
     * the two paths that reach it disagree on the version:
     *
     *  - `bcpg-jdk15on:1.69`, via `remote-dev-util`, requires
     *    `bcprov-jdk15on:1.69`;
     *  - `marketplace-zip-signer:0.1.3` (see [droppedModules]) requires
     *    `bcpkix-jdk15on:1.64`, which requires `bcprov-jdk15on:1.64`.
     *
     * A consumer resolving with `failOnVersionConflict()` cannot build
     * against such a POM without forcing a version of its own, which is
     * exactly the per-consumer boilerplate the POM is meant to remove.
     */
    private val droppedGroups = setOf(
        "org.jetbrains.kotlin",
        "org.jetbrains.kotlinx",
        "org.jetbrains.pty4j",
        "org.jetbrains.jediterm",
        "org.jvnet.winp",
        "org.bouncycastle",
    )

    /**
     * Individual artifacts deliberately absent from both the shade and
     * the POM, identified as `group:name`.
     *
     * Unlike [droppedGroups], these belong to groups whose other artifacts
     * are declared: `org.jetbrains` also holds `annotations`, which consumers
     * do need.
     *
     * `marketplace-zip-signer` signs plugin ZIP archives for the JetBrains
     * Marketplace. It arrives through `ide-impl`, and headless PSI signs
     * nothing. Declaring it would also bring the older half of
     * the BouncyCastle version clash described in [droppedGroups].
     */
    private val droppedModules = setOf(
        "org.jetbrains:marketplace-zip-signer",
    )

    /**
     * Tells if the artifacts of the given Maven [group] are welded
     * into the uber JARs.
     */
    fun isShaded(group: String): Boolean =
        shadedGroups.any { it.matches(group) }

    /**
     * Tells if the artifact with the given Maven [group] and [name] belongs
     * to the published POM as a `runtime` dependency.
     */
    fun isPomDependency(group: String, name: String): Boolean =
        !isShaded(group)
                && group !in droppedGroups
                && "$group:$name" !in droppedModules

    /**
     * Applies [relocations] to the given [ShadowJar] task.
     */
    fun relocateForks(task: ShadowJar) {
        relocations.forEach { (from, to) ->
            task.relocate(from, to)
        }
    }

    /**
     * Maps an entry path of an already-built uber JAR back to the form Shadow
     * sees before applying its transformations, returning `null` for paths
     * Shadow does not transform.
     *
     * The subtraction filter of `intellij-platform-java` inspects the entries
     * of the already-built `intellij-platform` JAR, where Shadow has already
     * transformed the paths. Shadow evaluates exclusion predicates against
     * the untransformed source paths, so the subtraction must also exclude
     * the source form of every transformed entry. Three transformations
     * apply when relocation is on:
     *
     *  1. Classes and resources of [relocated][relocations] packages move
     *     to the new package path.
     *  2. `META-INF/services/` files are renamed after the relocated
     *     service interface.
     *  3. Kotlin module files (`.kotlin_module`) under `META-INF`
     *     gain a `.shadow` infix.
     */
    fun sourceFormOf(path: String): String? {
        unrelocatedPathOf(path)?.let {
            return it
        }
        if (path.startsWith(SERVICES_DIR)) {
            return unrelocatedServiceFileOf(path)
        }
        val renamed = ".shadow$KOTLIN_MODULE_EXTENSION"
        if (path.endsWith(renamed)) {
            return path.removeSuffix(renamed) + KOTLIN_MODULE_EXTENSION
        }
        return null
    }

    private const val SERVICES_DIR = "META-INF/services/"
    private const val KOTLIN_MODULE_EXTENSION = ".kotlin_module"

    /**
     * Reverses transformation 1 of [sourceFormOf]: a slash-separated path
     * under a relocated package returns to the original package path.
     */
    private fun unrelocatedPathOf(path: String): String? {
        for ((source, target) in relocations) {
            val targetPath = target.replace('.', '/') + "/"
            if (path.startsWith(targetPath)) {
                val sourcePath = source.replace('.', '/') + "/"
                return sourcePath + path.removePrefix(targetPath)
            }
        }
        return null
    }

    /**
     * Reverses transformation 2 of [sourceFormOf]: a service file named after
     * a relocated interface returns to the name of the original interface.
     */
    private fun unrelocatedServiceFileOf(path: String): String? {
        val serviceName = path.removePrefix(SERVICES_DIR)
        for ((source, target) in relocations) {
            if (serviceName.startsWith("$target.")) {
                return SERVICES_DIR + source + serviceName.removePrefix(target)
            }
        }
        return null
    }
}

/**
 * Restricts the shade to JetBrains-produced artifacts and relocates
 * the JetBrains forks claiming public package names.
 *
 * Artifacts outside the [shaded groups][IntelliJUberJar.isShaded] do not
 * enter the JAR. Those of them available on Maven Central are declared as
 * POM `runtime` dependencies via [declareUnshadedDependencies].
 */
fun ShadowJar.shadeOnlyJetBrainsArtifacts() {
    dependencies {
        include { IntelliJUberJar.isShaded(it.moduleGroup) }
    }
    IntelliJUberJar.relocateForks(this)
}

/**
 * Declares the artifacts of the module's runtime classpath that are not
 * welded into the uber JAR as dependencies of the published POM.
 *
 * Dependencies on sibling uber-JAR modules, arriving as project dependencies,
 * receive the `compile` scope: the classes of this JAR compile against the
 * classes of the sibling, so a consumer compiling directly against this
 * artifact needs the sibling on the compile classpath, too. Third-party
 * artifacts receive the `runtime` scope, which serves the purpose of their
 * declaration — correct runtime resolution — without widening the compile
 * classpath beyond what the previously dependency-less POM provided.
 * The versions are those resolved in this project, i.e. the ones declared
 * by the IntelliJ Platform POMs; consumer projects upgrade them further
 * via the standard Gradle conflict resolution.
 *
 * The publication must not declare dependencies of its own, as those created
 * by `uber-jar-module` do not: this function appends a new `dependencies`
 * block to the POM without merging into an existing one.
 */
fun MavenPublication.declareUnshadedDependencies(project: Project) {
    // Look up the configuration eagerly: the deferred action below must not
    // capture the `Project` instance. This alone does not make the task
    // configuration-cache-ready — serializing a `Configuration` is equally
    // unsupported, and this repository does not enable that cache.
    val runtimeClasspath = project.configurations.getByName("runtimeClasspath")
    pom.withXml {
        val dependencies = asNode().appendNode("dependencies")
        runtimeClasspath.resolvedConfiguration.resolvedArtifacts
            .mapNotNull { artifact ->
                val fromProject =
                    artifact.id.componentIdentifier is ProjectComponentIdentifier
                val id = artifact.moduleVersion.id
                when {
                    fromProject -> id to "compile"
                    IntelliJUberJar.isPomDependency(id.group, id.name) -> id to "runtime"
                    else -> null
                }
            }
            .distinctBy { (id, _) -> "${id.group}:${id.name}" }
            .sortedBy { (id, _) -> "${id.group}:${id.name}" }
            .forEach { (id, scope) ->
                with(dependencies.appendNode("dependency")) {
                    appendNode("groupId", id.group)
                    appendNode("artifactId", id.name)
                    appendNode("version", id.version)
                    appendNode("scope", scope)
                }
            }
    }
}
