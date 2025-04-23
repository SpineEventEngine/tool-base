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

package io.spine.tools.gradle

import com.google.common.collect.ImmutableList
import io.spine.annotation.Internal
import kotlin.reflect.KClass
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty

/**
 * A Gradle [ListProperty] which is automatically instantiated by
 * the [ObjectFactory][org.gradle.api.model.ObjectFactory] of the associated project.
 *
 * @param E the type of the elements in the list.
 * @param project
 *         the project which provides an object factory for creating Gradle properties.
 * @param klass
 *         the class of the elements in the list.
 */
@Internal
public class Ordered<E : Any>(project: Project, klass: KClass<E>) :
    ListProperty<E> by project.objects.listProperty(klass.java) {

    /**
     * Creates a new `Ordered`.
     *
     * This is a Java-friendly constructor. Use the primary constructor in Kotlin.
     *
     * @param project
     *         the project which provides an object factory for creating Gradle properties.
     * @param cls
     *         the Java class of the elements in the list.
     */
    public constructor(project: Project, cls: Class<E>) : this(project, cls.kotlin)

    /**
     * Obtains the value of this property and applies the given mapping function to each element.
     *
     * @param T the target type of the transformation.
     */
    public fun <T : Any> transform(transformer: (E) -> T): ImmutableList<T> =
        get().map(transformer).toImmutableList()
}

private fun <T : Any> Iterable<T>.toImmutableList(): ImmutableList<T> = ImmutableList.copyOf(this)
