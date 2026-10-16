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
 * `ProtobufRuntimeVersionException`. Declared as a dependency, the runtime takes
 * part in the dependency resolution of the consumer, so the version the consumer
 * resolves wins.
 *
 * The libraries are declared:
 *  1. in the `shadow` configuration, from which the Shadow plugin writes the dependencies
 *     of the shadowed variant into the metadata of the Gradle plugin publication;
 *  2. in the POM of the `fatJar` publication of `uber-jar-module`, which publishes
 *     the uber JAR with `artifact(...)` and so receives no dependencies from Gradle.
 *
 * Each library is declared non-transitively. Its own dependencies are either declared
 * here too (`protobuf-java`), or bundled into the uber JAR (Guava, Gson), or provided
 * by the Gradle runtime (the Kotlin runtime). So the uber JAR reaches consumers
 * along with the same classes as before, except that those of the Protobuf runtime
 * come from genuine artifacts.
 *
 * The libraries are excluded by the dependency filter of the `shadowJar` task rather
 * than by the paths of their entries, so that the SBOM published with the uber JAR
 * does not describe them as bundled.
 */
fun Project.unbundleProtobufRuntime() {
    Protobuf.libs.forEach {
        dependencies.add("shadow", it) {
            isTransitive = false
        }
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
 * The publication must not declare dependencies of its own, as those created
 * by `uber-jar-module` do not: this function appends a new `dependencies` block
 * to the POM without merging into an existing one.
 */
private fun MavenPublication.declareRuntimeDependencies(coordinates: List<String>) {
    pom.withXml {
        val dependencies = asNode().appendNode("dependencies")
        coordinates.forEach {
            val (group, name, version) = it.split(':')
            with(dependencies.appendNode("dependency")) {
                appendNode("groupId", group)
                appendNode("artifactId", name)
                appendNode("version", version)
                appendNode("scope", "runtime")
                with(appendNode("exclusions").appendNode("exclusion")) {
                    appendNode("groupId", "*")
                    appendNode("artifactId", "*")
                }
            }
        }
    }
}
