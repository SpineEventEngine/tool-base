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

@file:Suppress("RemoveRedundantQualifierName")

import com.google.protobuf.gradle.id
import io.spine.internal.dependency.Grpc
import io.spine.internal.dependency.JavaPoet
import io.spine.internal.dependency.JavaX
import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.Spine

plugins {
    protobuf
    `java-test-fixtures`
    `detekt-code-analysis`
}

dependencies {
    api(JavaPoet.lib)
    api(JavaX.annotations)

    val spine = Spine(project)
    api(spine.base)
    implementation(spine.validation.runtime)

    listOf(
        Grpc.protobuf,
        Grpc.core,
        Grpc.stub,
        "io.grpc:grpc-kotlin-stub:1.3.0",
        spine.validation.runtime
    ).forEach {
        testImplementation(it)
        testFixturesImplementation(it)
    }

    testImplementation(spine.testlib)
}

sourceSets {
    testFixtures {
        java.srcDirs("$projectDir/generated/testFixtures/grpc")
    }
}

val generatedDir = "$projectDir/generated"

/**
 * Force `generated` directory and Kotlin code generation.
 */
protobuf {
    generatedFilesBaseDir = generatedDir

    protoc {
        artifact = Protobuf.compiler
    }

    plugins {
        id("grpc") {
            artifact = Grpc.protobufPlugin
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.3.0:jdk8@jar"
        }
    }

    generateProtoTasks.all().configureEach {
        builtins.maybeCreate("kotlin")
        plugins {
            id("grpc")
            id("grpckt")
        }
    }
}

tasks.clean.configure {
    delete(generatedDir)
}

applyGeneratedDirectories(generatedDir)

/**
 * Adds directories with the generated source code to source sets of the project and
 * to IntelliJ IDEA module settings.
 *
 * @param generatedDir
 *          the name of the root directory with the generated code
 */
fun Project.applyGeneratedDirectories(generatedDir: String) {
    val generatedMain = "$generatedDir/main"
    val generatedJava = "$generatedMain/java"
    val generatedKotlin = "$generatedMain/kotlin"
    val generatedGrpc = "$generatedMain/grpc"

    val generatedTest = "$generatedDir/test"
    val generatedTestJava = "$generatedTest/java"
    val generatedTestKotlin = "$generatedTest/kotlin"
    val generatedTestGrpc = "$generatedTest/grpc"

    sourceSets {
        main {
            java.srcDirs(
                generatedJava,
                generatedGrpc,
            )
            kotlin.srcDirs(
                generatedKotlin,
            )
        }
        test {
            java.srcDirs(
                generatedTestJava,
                generatedTestGrpc,
            )
            kotlin.srcDirs(
                generatedTestKotlin,
            )
        }
    }

    idea {
        module {
            generatedSourceDirs.addAll(files(
                generatedJava,
                generatedKotlin,
                generatedGrpc,
            ))
            testSources.from(
                generatedTestJava,
                generatedTestKotlin,
                generatedTestGrpc,
            )
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }
}

/**
 * Make the `sourcesJar` task accept duplicated input which seems to occur
 * somewhere inside either ProtoData or McJava.
 */
project.afterEvaluate {
    @Suppress("UNUSED_VARIABLE")
    val sourcesJar by tasks.getting {
        this as Jar
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
