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

package io.spine.tools.psi

import com.intellij.core.CoreApplicationEnvironment
import com.intellij.mock.MockComponentManager

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.extensions.ProjectExtensionPointName
import kotlin.reflect.KClass

/**
 * Registers the extension point with this extension area.
 *
 * @param T
 *         the type of the extension point implementation.
 * @param epName
 *         the name of the extension point.
 */
public inline fun <reified T: Any> ExtensionsArea.register(epName: ExtensionPointName<T>) {
    CoreApplicationEnvironment.registerExtensionPoint(this, epName, T::class.java)
}

/**
 * Registers the extension point and its implementation with this extension area.
 *
 * @param T
 *         the type of the extension point.
 * @param epName
 *         the name of the extension point.
 * @param impl
 *         the type of the extension point implementation.
 */
public inline fun <reified T: Any> ExtensionsArea.register(
    epName: ExtensionPointName<T>,
    impl: Class<out T>
) {
    CoreApplicationEnvironment.registerExtensionPoint(this, epName, impl)
}

/**
 * Registers the implementation of the services with this `MockComponentManager`.
 *
 * @param T
 *         the type of the service.
 * @param impl
 *         the class of the service implementation.
 */
public inline fun <reified T : Any> MockComponentManager.registerServiceImpl(impl: Class<out T>) {
    registerService(T::class.java, impl)
}

/**
 * Registers the implementation of the services with this `MockComponentManager`.
 *
 * @param T
 *         the type of the service.
 * @param impl
 *         the Kotlin class of the service implementation, Java instance of which will be used.
 */
public inline fun <reified T : Any> MockComponentManager.registerServiceImpl(impl: KClass<out T>) {
    registerService(T::class.java, impl.java)
}

/**
 * Replaces the implementation of the service.
 *
 * @param T
 *         the type of the service.
 * @param newImpl
 *         the class of the new service implementation to be used.
 */
public inline fun <reified T : Any> MockComponentManager.replaceServiceImpl(newImpl: Class<out T>) {
    picoContainer.unregisterComponent(T::class.java.name)
    registerServiceImpl(newImpl)
}

/**
 * Registers the given project extension point.
 *
 * @param T
 *         the type of the project extension point.
 * @param point
 *         the name of the project extension point.
 */
public inline fun <reified T : Any> MockComponentManager.registerPoint(
    point: ProjectExtensionPointName<T>
) {
    CoreApplicationEnvironment.registerExtensionPoint(extensionArea, point.name, T::class.java)
}
