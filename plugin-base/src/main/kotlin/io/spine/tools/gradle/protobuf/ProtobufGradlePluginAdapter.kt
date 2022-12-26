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

package io.spine.tools.gradle.protobuf

import com.google.protobuf.gradle.ExecutableLocator
import com.google.protobuf.gradle.GenerateProtoTask
import groovy.lang.Closure
import io.spine.tools.groovy.ConsumerClosure.closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskCollection

/**
 * Unified API for selected features of Protobuf Gradle Plugin for handling transition
 * from versions before `0.9.0` to `0.9.1`.
 */
public interface ProtobufGradlePluginAdapter {
    public val project: Project
    public var generatedFilesBaseDir: String
    public fun protoc(action: Action<ExecutableLocator>)
    public fun plugins(action: Action<NamedDomainObjectContainer<ExecutableLocator>>)
    public fun configureProtoTasks(action: Action<GenerateProtoTask>)
}

/**
 * Obtains the version-neutral API for selected features of Protobuf Gradle Plugin
 * to serve the transition from plugin version from pre-`0.9.0` to `0.9.1`.
 */
public val Project.protobufGradlePluginAdapter: ProtobufGradlePluginAdapter
    get() = AdapterImpl(this)

/**
 * The adapter for working with Protobuf Gradle Plugin in the given project.
 */
private class AdapterImpl(
    project: Project,
    private val delegate: ProtobufGradlePluginAdapter = createDelegate(project)
) : ProtobufGradlePluginAdapter by delegate {

    companion object {
        fun createDelegate(project: Project): ProtobufGradlePluginAdapter {
            val newApi = NewApi.findExtension(project) != null
            return if (newApi) NewApi(project) else ConvApi(project)
        }
    }
}

/**
 * Adapter for the API of Protobuf Gradle Plugin after v0.9.0.
 */
private class NewApi(override val project: Project): ProtobufGradlePluginAdapter {

    private val extension: Any = findExtension(project)!!
    private val extensionClass: Class<*> = extension.javaClass

    override var generatedFilesBaseDir: String
        get() {
            val getter = extensionClass.getMethod("getGeneratedFilesBaseDir")
            return getter.invoke(extension) as String
        }

        set(value) {
            val setter = extensionClass.getMethod("setGeneratedFilesBaseDir", String::class.java)
            setter.invoke(extension, value)
        }

    override fun protoc(action: Action<ExecutableLocator>) {
        val setter = extensionClass.getMethod("protoc")
        setter.invoke(extension, action)
    }

    override fun plugins(action: Action<NamedDomainObjectContainer<ExecutableLocator>>) {
        val pluginsMethod = extensionClass.getMethod("plugins", Action::class.java)
        pluginsMethod.invoke(extension, action)
    }

    override fun configureProtoTasks(action: Action<GenerateProtoTask>) {
        val generateProtoTasks = extensionClass.getMethod("generateProtoTasks", Action::class.java)

        // The actual type on which the closure operates (in newer API) is
        // `ProtobufExtension.GenerateProtoTaskCollection`. We cannot use this type because
        // it may not be available if older Gradle plugin is applied. We'd have
        // `ProtobufConfigurator.GenerateProtoTaskCollection` instead. Therefore, we obtain
        // an instance of `all()` tasks via a reflection call on the closure argument.
        val closure = closure<Any> {
            val all = it.javaClass.getMethod("all")
            val allTasks = all.invoke(it)
            @Suppress("UNCHECKED_CAST")
            (allTasks as TaskCollection<GenerateProtoTask>).forEach { task ->
                action.execute(task)
            }
        }
        // Now pass the closure for the Protobuf Gradle plugin for being applied later.
        generateProtoTasks.invoke(extension, closure)
    }

    companion object {
        const val extensionName = "protobuf"

        fun findExtension(project: Project): Any? =
            project.extensions.findByName(extensionName)
    }
}

/**
 * Adapter for the API of Protobuf Gradle Plugin pre v0.9.0.
 */
private class ConvApi(override val project: Project): ProtobufGradlePluginAdapter {

    /**
     * The convention object attached to a Gradle Project by the Protobuf Gradle Plugin.
     *
     * This Groovy object has the type `ProtobufConvention` and is statically compiled.
     */
    @Suppress("DEPRECATION") /* Still have to use until migration to v0.9.1 is complete. */
    private val convention: Any = project.convention.plugins["protobuf"]!!

    /**
     * The class of the
     */
    private val conventionsClass = convention.javaClass

    /**
     * The value of the `ProtobufConvention.protobuf` property, which has
     * the type `ProtobufConfigurator`. This is a statically compiled Groovy object.
     */
    private val protobufConfigurator: Any

    /**
     * The class of the `ProtobufConfigurator` for reflection access.
     */
    private val protobufConfiguratorClass: Class<*>

    init {
        // Accessor for the `ProtobufConvention.protobuf` property.
        val getProtobuf = conventionsClass.getMethod("getProtobuf")
        protobufConfigurator = getProtobuf.invoke(convention)
        protobufConfiguratorClass = protobufConfigurator.javaClass
    }

    override var generatedFilesBaseDir: String
        get() {
            val getGeneratedFilesBaseDirField = protobufConfiguratorClass.getMethod(
                "getGeneratedFilesBaseDir"
            )
            return getGeneratedFilesBaseDirField.invoke(protobufConfigurator) as String
        }
        set(value) {
            val setGeneratedFilesBaseDirField = protobufConfiguratorClass.getMethod(
                "setGeneratedFilesBaseDir", String::class.java
            )
            setGeneratedFilesBaseDirField.invoke(protobufConfigurator, value)
        }

    override fun protoc(action: Action<ExecutableLocator>) {
        val protocMethod = protobufConfiguratorClass.getMethod("protoc", Closure::class.java)
        val closure = closure<ExecutableLocator> { l -> action.execute(l) }
        protocMethod.invoke(protobufConfigurator, closure)
    }

    override fun plugins(action: Action<NamedDomainObjectContainer<ExecutableLocator>>) {
        val plugins = protobufConfiguratorClass.getMethod("plugins", Closure::class.java)
        val closure = closure<NamedDomainObjectContainer<ExecutableLocator>> { l ->
            action.execute(l)
        }
        plugins.invoke(protobufConfigurator, closure)
    }

    override fun configureProtoTasks(action: Action<GenerateProtoTask>) {
        val generateProtoTasks = protobufConfiguratorClass.getMethod(
            "generateProtoTasks", Closure::class.java
        )
        // The actual type on which the closure operates (in older API) is
        // `ProtobufConfigurator.GenerateProtoTaskCollection`. We cannot use this type because
        // in newer API type is no longer available, and we'd have
        // `ProtobufExtension.GenerateProtoTaskCollection` instead. Therefore, we obtain
        // an instance of `all()` tasks via a reflection call on the closure argument.
        val closure = closure<Any> {
            val all = it.javaClass.getMethod("all")
            val allTasks = all.invoke(it)
            @Suppress("UNCHECKED_CAST")
            (allTasks as TaskCollection<GenerateProtoTask>).forEach { task ->
                action.execute(task)
            }
        }
        // Now pass the closure for the Protobuf Gradle plugin for being applied later.
        generateProtoTasks.invoke(protobufConfigurator, closure)
    }
}
