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

import groovy.util.Node
import io.spine.internal.dependency.ProtoData
import io.spine.internal.gradle.publish.SpinePublishing

plugins {
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

/** The publishing settings from the root project. */
val spinePublishing = rootProject.the<SpinePublishing>()

/**
 * The ID of the far JAR artifact.
 *
 * This value is also used in `io.spine.tools.mc.java.gradle.Artifacts.kt`.
 */
val projectArtifact = spinePublishing.artifactPrefix + "psi-java-bundle"

val protoDataVersion: String by extra

dependencies {
    implementation(project(":psi-java"))
}


publishing {
    val groupName = project.group.toString()
    val versionName = project.version.toString()

    publications {
        create("fatJar", MavenPublication::class) {
            groupId = groupName
            artifactId = projectArtifact
            version = versionName
            artifact(tasks.shadowJar)

            /**
             * Manually add the dependency onto `io.spine:protodata`,
             * as there is no good way to remove all the dependencies
             * from the fat JAR artifact, but leave just this one.
             *
             * This dependency is required in order to place the ProtoData plugin
             * onto the build classpath, so that `mc-java` routines
             * could apply it programmatically.
             *
             * The appended content should look like this:
             * ```
             *     <dependency>
             *         <groupId>io.spine</groupId>
             *         <artifactId>protodata</artifactId>
             *         <version>$protoDataVersion</version>
             *         <scope>runtime</scope>
             *         <exclusions>
             *              <exclusion>
             *                  <groupId>io.spine.protodata</groupId>
             *                  <artifactId>*</artifactId>
             *              </exclusion>
             *              <exclusion>
             *                  <groupId>org.jetbrains.kotlin</groupId>
             *                  <artifactId>*</artifactId>
             *              </exclusion>
             *              <exclusion>
             *                  <groupId>com.google.protobuf</groupId>
             *                  <artifactId>*</artifactId>
             *              </exclusion>
             *              <exclusion>
             *                  <groupId>io.spine.tools</groupId>
             *                  <artifactId>*</artifactId>
             *              </exclusion>
             *         </exclusions>
             *    </dependency>
             * ```
             */
            pom.withXml {
                val projectNode: Node = asNode() as Node
                val dependencies = Node(projectNode, "dependencies")
                val dependency = Node(dependencies, "dependency")

                val exclusions = Node(dependency, "exclusions")
                excludeGroupId(exclusions, "org.jetbrains.kotlin")
                excludeGroupId(exclusions, "com.google.protobuf")
                excludeGroupId(exclusions, "io.spine.tools")
            }
        }
    }
}

/**
 * Declare dependency explicitly to address the Gradle warning.
 */
val publishFatJarPublicationToMavenLocal: Task by tasks.getting {
    dependsOn(tasks.jar)
    println("Task `${this.name}` now depends on `${tasks.jar.name}`.")
}

tasks.publish {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    exclude(
        /*
          Exclude IntelliJ Platform images and other resources associated with IntelliJ UI.
          We do not call the UI, so they won't be used.
         */
        "actions/**",
        "chooser/**",
        "codeStyle/**",
        "codeStylePreview/**",
        "codeWithMe/**",
        "darcula/**",
        "debugger/**",
        "diff/**",
        "duplicates/**",
        "expui/**",
        "extensions/**",
        "fileTemplates/**",
        "fileTypes/**",
        "general/**",
        "graph/**",
        "gutter/**",
        "hierarchy/**",
        "icons/**",
        "ide/**",
        "idea/**",
        "inlayProviders/**",
        "inspectionDescriptions/**",
        "inspectionReport/**",
        "intentionDescriptions/**",
        "javadoc/**",
        "javaee/**",
        "json/**",
        "liveTemplates/**",
        "mac/**",
        "modules/**",
        "nodes/**",
        "objectBrowser/**",
        "plugins/**",
        "postfixTemplates/**",
        "preferences/**",
        "process/**",
        "providers/**",
        "runConfigurations/**",
        "scope/**",
        "search/**",
        "toolbar/**",
        "toolbarDecorator/**",
        "toolwindows/**",
        "vcs/**",
        "webreferences/**",
        "welcome/**",
        "windows/**",
        "xml/**",

        /*
          Exclude `https://github.com/JetBrains/pty4j`.
          We don't need the terminal.
         */
        "resources/com/pti4j/**",

        /* Exclude the IntelliJ fork of
          `http://www.sparetimelabs.com/purejavacomm/purejavacomm.php`.
           It is the part of the IDEA's terminal implementation.
         */
        "purejavacomm/**",

        /* Exclude IDEA project templates. */
        "resources/projectTemplates/**",

        /*
          Exclude dynamic libraries. Should the tool users need them,
          they would add them explicitly.
         */
        "bin/**",

        /*
          Exclude Google Protobuf definitions to avoid duplicates.
         */
        "google/**",
        "src/google/**",
        
        /**
         * Exclude Spine Protobuf definitions to avoid duplications.
         */
        "spine/**",

        /**
         * Exclude Kotlin runtime because it will be provided.
         */
        "kotlin/**",
        "kotlinx/**",

        /**
         * Exclude native libraries related to debugging.
         */
        "win32-x86/**",
        "win32-x86-64/**",

        /**
         * Exclude Windows process management library (WinP).
         * `https://github.com/jenkinsci/winp`.
         */
        "winp.dll",
        "winp.x64.dll"
    )

    setZip64(true)  /* The archive has way too many items. So using the Zip64 mode. */
    archiveClassifier.set("all")    /** To prevent Gradle setting something like `osx-x86_64`. */
    mergeServiceFiles("desc.ref")
    mergeServiceFiles("META-INF/services/io.spine.option.OptionsProvider")
}

// See https://github.com/johnrengelman/shadow/issues/153.
//tasks.shadowDistTar.get().enabled = false
//tasks.shadowDistZip.get().enabled = false
//tasks.distTar.get().enabled = false
//tasks.distZip.get().enabled = false

fun excludeGroupId(exclusions: Node, groupId: String) {
    val exclusion = Node(exclusions, "exclusion")
    Node(exclusion, "groupId", groupId)
    Node(exclusion, "artifactId", "*")
}

