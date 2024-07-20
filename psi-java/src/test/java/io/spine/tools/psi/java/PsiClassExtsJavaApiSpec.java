/*
 * Copyright 2024, TeamDev. All rights reserved.
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

package io.spine.tools.psi.java;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.psi.java.PsiClassExtsKt.implement;
import static io.spine.tools.psi.java.PsiCommandsKt.execute;
import static io.spine.tools.psi.java.PsiClassExtsKt.doesImplement;
import static io.spine.tools.psi.java.PsiElementFactoryExtsKt.createClassReference;
import static io.spine.tools.psi.java.PsiModifierListOwnerExtsKt.removePublic;

@DisplayName("`PsiClass` extensions Java API should")
class PsiClassExtsJavaApiSpec {

    private static final PsiElementFactory elementFactory =
            Environment.INSTANCE.getElementFactory();

    private PsiClass cls;

    private final PsiJavaCodeReferenceElement runnable =
            createClassReference(elementFactory, null, Runnable.class.getCanonicalName());

    @BeforeEach
    void createClass() {
        cls = elementFactory.createClass("Stub");
        // Remove the `public` modifier automatically added by the `ElementFactory`.
        // We need a "bare-bones" class in the tests.
        execute(() -> removePublic(cls));
    }

    @Test
    @DisplayName("provide alias for `implements` function")
    void methodAlias() {
        assertThat(doesImplement(cls, runnable)).isFalse();

        execute(() -> implement(cls, runnable));

        assertThat(doesImplement(cls, runnable)).isTrue();
    }
}
