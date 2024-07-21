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

package io.spine.tools.psi.java

import com.google.errorprone.annotations.CanIgnoreReturnValue
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifier.FINAL
import com.intellij.psi.PsiModifier.PRIVATE
import com.intellij.psi.PsiModifier.PUBLIC
import com.intellij.psi.PsiModifier.STATIC
import com.intellij.psi.PsiModifierList
import com.intellij.psi.PsiModifierListOwner
import org.jetbrains.annotations.NonNls

/**
 * Tells if this object has the modifier with the given [name].
 */
public fun PsiModifierListOwner.hasModifier(
    @PsiModifier.ModifierConstant @NonNls name: String): Boolean {
    return  modifierList?.hasModifierProperty(name) ?: false
}

/**
 * Returns `true` if the modifier [`final`][FINAL] is applied.
 */
public val PsiModifierListOwner.isFinal: Boolean
    get() = hasModifier(FINAL)

/**
 * Returns `true` if the modifier [`public`][PUBLIC] is applied.
 */
public val PsiModifierListOwner.isPublic: Boolean
    get() = hasModifier(PUBLIC)

/**
 * Returns `true` if the modifier [`private`][PRIVATE] is applied.
 */
public val PsiModifierListOwner.isPrivate: Boolean
    get() = hasModifier(PRIVATE)

/**
 * Returns `true` if the modifier [`static`][STATIC] is applied.
 */
public val PsiModifierListOwner.isStatic: Boolean
    get() = hasModifier(STATIC)


/**
 * Adds the [`public`][PUBLIC] modifier.
 */
@CanIgnoreReturnValue
public fun PsiModifierListOwner.makePublic(): PsiModifierListOwner {
    modifierList?.setIfAbsent(PUBLIC)
    return this
}

/**
 * Adds the [`final`][FINAL] modifier.
 */
@CanIgnoreReturnValue
public fun PsiModifierListOwner.makeFinal(): PsiModifierListOwner {
    modifierList?.setIfAbsent(FINAL)
    return this
}

/**
 * Adds the [`static`][STATIC] modifier.
 */
@CanIgnoreReturnValue
public fun PsiModifierListOwner.makeStatic(): PsiModifierListOwner {
    modifierList?.setIfAbsent(STATIC)
    return this
}

/**
 * Removes the [`public`][PUBLIC] modifier.
 */
@CanIgnoreReturnValue
public fun PsiModifierListOwner.removePublic(): PsiModifierListOwner {
    modifierList?.setModifierProperty(PUBLIC, false)
    return this
}

/**
 * Adds the given modifier to the list if it is not yet added.
 */
public fun PsiModifierList.setIfAbsent(
    @PsiModifier.ModifierConstant @NonNls modifier: String
) {
    if (!hasModifierProperty(modifier)) {
        setModifierProperty(modifier, true)
    }
}
