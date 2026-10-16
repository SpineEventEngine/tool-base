/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package io.spine.gradle.shade

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.spine.dependency.lib.Protobuf
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named

/**
 * Leaves the Protobuf runtime ([Protobuf.libs]) out of the uber JAR of this module,
 * declaring it as a `runtime` dependency of the published artifacts instead.
 *
 * A bundled copy of the runtime cannot be relocated, as the runtime depends on
 * the names of its classes. Left unrelocated, it shadows the Protobuf runtime that
 * a consumer resolves for its own build classpath, and no `force(...)` can replace it:
 * the copy is bytecode inside another artifact, not a dependency. Should the copy be
 * older than the code generated for the consumer, loading that code fails with
 * `ProtobufRuntimeVersionException`. Declared as a dependency, the runtime takes part in
 * the dependency resolution of the consumer, so the version the consumer resolves wins.
 *
 * The libraries are declared:
 *  1. in the `protobufRuntime` configuration, which the `shadowRuntimeElements` variant
 *     extends, so that the metadata of the Gradle plugin publication lists them;
 *  2. in the POM of the `fatJar` publication of `uber-jar-module`, which publishes
 *     the uber JAR with `artifact(...)` and so receives no dependencies from Gradle.
 *
 * The `shadow` configuration is not used for the purpose: Shadow writes its files into
 * the `Class-Path` attribute of the JAR manifest. Such entries are relative to the JAR
 * and carry the version of this build in the file names, so in a directory holding
 * the JAR along with other Protobuf JARs, they would put the runtime of this build
 * first again.
 *
 * Each library is declared non-transitively. Its own dependencies are declared here too
 * (`protobuf-java`), bundled into the uber JAR (Guava, Gson), or provided by the Gradle
 * runtime (the Kotlin runtime). So consumers get the same classes as before, except that
 * those of the Protobuf runtime come from genuine artifacts.
 *
 * The libraries are excluded by the dependency filter of the `shadowJar` task rather
 * than by the paths of their entries, so that the SBOM published with the uber JAR
 * does not describe them as bundled.
 *
 * Requires the `uber-jar-module` plugin, which brings the `shadowJar` task and
 * the `fatJar` publication.
 */
fun Project.unbundleProtobufRuntime() {
    val protobufRuntime = configurations.create("protobufRuntime") {
        description = "The Protobuf runtime, which the published artifacts declare" +
                " rather than bundle."
        isCanBeConsumed = false
        isCanBeResolved = false
    }
    Protobuf.libs.forEach {
        dependencies.add(protobufRuntime.name, it) {
            isTransitive = false
        }
    }
    configurations.named("shadowRuntimeElements") {
        extendsFrom(protobufRuntime)
    }
    tasks.named<ShadowJar>("shadowJar") {
        dependencies {
            exclude { "${it.moduleGroup}:${it.moduleName}" in protobufRuntimeModules }
        }
    }
    extensions.getByType<PublishingExtension>()
        .publications
        .named<MavenPublication>("fatJar") {
            declareRuntimeDependencies(Protobuf.libs)
        }
}

/**
 * The `group:name` coordinates of the Protobuf runtime libraries.
 */
private val protobufRuntimeModules: Set<String> =
    Protobuf.libs.mapTo(mutableSetOf()) { it.substringBeforeLast(':') }

/**
 * Declares the dependencies on the given `group:name:version` [coordinates]
 * in the POM of this publication, in the `runtime` scope and without
 * transitive dependencies.
 *
 * The publication must not declare dependencies of its own, which holds for the `fatJar`
 * publication of `uber-jar-module`: this function appends a new `dependencies` block
 * to the POM without merging into an existing one.
 */
private fun MavenPublication.declareRuntimeDependencies(coordinates: List<String>) {
    pom.withXml {
        val dependencies = asNode().appendNode("dependencies")
        coordinates.forEach {
            // Not `name` and `version`, which would hide those of the publication.
            val (libGroup, libName, libVersion) = it.split(':')
            with(dependencies.appendNode("dependency")) {
                appendNode("groupId", libGroup)
                appendNode("artifactId", libName)
                appendNode("version", libVersion)
                appendNode("scope", "runtime")
                with(appendNode("exclusions").appendNode("exclusion")) {
                    appendNode("groupId", "*")
                    appendNode("artifactId", "*")
                }
            }
        }
    }
}
