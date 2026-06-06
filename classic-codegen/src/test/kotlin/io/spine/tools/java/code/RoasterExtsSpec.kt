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

package io.spine.tools.java.code

import io.kotest.matchers.shouldBe
import org.jboss.forge.roaster.model.JavaDoc
import org.jboss.forge.roaster.model.JavaDocTag
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Kotlin extension for `JavaDoc` should")
internal class RoasterExtsSpec {

    @Test
    fun `collapse double spaces and tighten the closing brace before a period`() {
        val doc = StubJavaDoc("a  b } .")

        doc.fullTextNormalized() shouldBe "a b }."
    }

    @Test
    fun `leave already normalized text intact`() {
        val doc = StubJavaDoc("Just a sentence.")

        doc.fullTextNormalized() shouldBe "Just a sentence."
    }

    /**
     * A hand-written stub of [JavaDoc] exposing the given [full] text as its
     * [full text][JavaDoc.getFullText]. All other members are not exercised by
     * the extension under test.
     */
    private class StubJavaDoc(private val full: String) : JavaDoc<Any> {
        override fun getText(): String = full
        override fun getFullText(): String = full
        override fun getTagNames(): Set<String> = emptySet()
        override fun getTags(tagName: String): List<JavaDocTag> = emptyList()
        override fun getTags(): List<JavaDocTag> = emptyList()
        override fun getInternal(): Any = this
        override fun getOrigin(): Any = this
    }
}
