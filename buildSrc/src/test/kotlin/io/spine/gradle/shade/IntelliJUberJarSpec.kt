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

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`IntelliJUberJar` should")
internal class IntelliJUberJarSpec {

    @Test
    fun `shade JetBrains groups`() {
        IntelliJUberJar.isShaded("com.jetbrains.intellij.platform") shouldBe true
        IntelliJUberJar.isShaded("com.jetbrains.intellij.java") shouldBe true
        IntelliJUberJar.isShaded("org.jetbrains.intellij.deps") shouldBe true
        IntelliJUberJar.isShaded("org.jetbrains.intellij.deps.jna") shouldBe true
        IntelliJUberJar.isShaded("org.jetbrains.intellij.deps.batik") shouldBe true
        // The group of `blockmap`, which is absent from Maven Central.
        IntelliJUberJar.isShaded("org.jetbrains.intellij") shouldBe true
    }

    @Test
    fun `not shade third-party groups`() {
        IntelliJUberJar.isShaded("org.jdom") shouldBe false
        IntelliJUberJar.isShaded("com.google.guava") shouldBe false
        IntelliJUberJar.isShaded("org.jetbrains.kotlin") shouldBe false
        IntelliJUberJar.isShaded("org.jetbrains.pty4j") shouldBe false
        // The group of `org.jetbrains:annotations` is not `org.jetbrains.intellij`.
        IntelliJUberJar.isShaded("org.jetbrains") shouldBe false
        // The group regexes are anchored: a lookalike prefix does not match.
        IntelliJUberJar.isShaded("com.jetbrains.intellijoid") shouldBe false
    }

    @Test
    fun `declare only unshaded, non-dropped groups in the POM`() {
        IntelliJUberJar.isPomDependency("com.google.guava", "guava") shouldBe true
        IntelliJUberJar.isPomDependency("org.apache.commons", "commons-compress") shouldBe true
        IntelliJUberJar.isPomDependency("org.jdom", "jdom") shouldBe true

        // Shaded groups do not belong to the POM.
        IntelliJUberJar.isPomDependency("com.jetbrains.intellij.platform", "ide-impl") shouldBe
                false
        IntelliJUberJar.isPomDependency("org.jetbrains.intellij.deps", "jdom") shouldBe false

        // Deliberately dropped groups do not belong to the POM either.
        IntelliJUberJar.isPomDependency("org.jetbrains.kotlin", "kotlin-stdlib") shouldBe false
        IntelliJUberJar.isPomDependency("org.jetbrains.kotlinx", "kotlinx-serialization-core-jvm")
            .shouldBe(false)
        IntelliJUberJar.isPomDependency("org.jetbrains.pty4j", "pty4j") shouldBe false
        IntelliJUberJar.isPomDependency("org.jetbrains.jediterm", "jediterm-core") shouldBe false
        IntelliJUberJar.isPomDependency("org.jvnet.winp", "winp") shouldBe false

        // The BouncyCastle stack is dropped as a whole, so that the POM does not
        // carry the `1.69` vs. `1.64` clash of its two entry points.
        IntelliJUberJar.isPomDependency("org.bouncycastle", "bcpg-jdk15on") shouldBe false
        IntelliJUberJar.isPomDependency("org.bouncycastle", "bcprov-jdk15on") shouldBe false
        IntelliJUberJar.isPomDependency("org.bouncycastle", "bcpkix-jdk15on") shouldBe false
    }

    @Test
    fun `drop an individual artifact, keeping the rest of its group`() {
        IntelliJUberJar.isPomDependency("org.jetbrains", "marketplace-zip-signer") shouldBe false

        // The group of the dropped artifact stays declarable.
        IntelliJUberJar.isPomDependency("org.jetbrains", "annotations") shouldBe true
    }

    @Test
    fun `map a relocated class path back to its source form`() {
        IntelliJUberJar.sourceFormOf("io/spine/tools/ij/jdom/Element.class") shouldBe
                "org/jdom/Element.class"
        IntelliJUberJar.sourceFormOf("io/spine/tools/ij/log4j/Logger.class") shouldBe
                "org/apache/log4j/Logger.class"
        IntelliJUberJar.sourceFormOf(
            "io/spine/tools/ij/unimi/dsi/fastutil/ints/IntList.class"
        ) shouldBe "it/unimi/dsi/fastutil/ints/IntList.class"
    }

    @Test
    fun `map a renamed service file back to its source form`() {
        IntelliJUberJar.sourceFormOf(
            "META-INF/services/io.spine.tools.ij.batik.ext.awt.image.spi.ImageWriter"
        ) shouldBe "META-INF/services/org.apache.batik.ext.awt.image.spi.ImageWriter"
    }

    @Test
    fun `map a renamed Kotlin module file back to its source form`() {
        IntelliJUberJar.sourceFormOf("META-INF/intellij.platform.core.shadow.kotlin_module")
            .shouldBe("META-INF/intellij.platform.core.kotlin_module")
    }

    @Test
    fun `return 'null' for paths Shadow does not transform`() {
        IntelliJUberJar.sourceFormOf("com/intellij/psi/PsiFile.class") shouldBe null
        IntelliJUberJar.sourceFormOf("META-INF/MANIFEST.MF") shouldBe null
        // A service file of an interface outside the relocated packages.
        IntelliJUberJar.sourceFormOf(
            "META-INF/services/org.codehaus.stax2.XMLInputFactory2"
        ) shouldBe null
        // A Kotlin module file that was not renamed.
        IntelliJUberJar.sourceFormOf("META-INF/blockmap.kotlin_module") shouldBe null
    }
}
