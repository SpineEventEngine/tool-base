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
import java.lang.reflect.Field
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskCollection

/**
 * Unified API for working with Protobuf Gradle Plugin before and after 0.9.0.
 */
internal interface ProtobufGradlePluginApi {
    val project: Project
    var generatedFilesBaseDir: String
    val generateProtoTasksAll: TaskCollection<GenerateProtoTask>
    fun protoc(action: Action<ExecutableLocator>)
    fun plugins(action: Action<NamedDomainObjectContainer<ExecutableLocator>>)
}

/**
 * The adapter for working with Protobuf Gradle Plugin in the given project.
 */
internal class ProtobufGradlePluginAdapter(
    project: Project,
    private val delegate: ProtobufGradlePluginApi = createDelegate(project)
) : ProtobufGradlePluginApi by delegate {

    companion object {
        fun createDelegate(project: Project): ProtobufGradlePluginApi {
            val newApi = NewApi.findExtension(project) != null
            return if (newApi) NewApi(project) else ConvApi(project)
        }
    }
}

/**
 * Adapter for the API of Protobuf Gradle Plugin after v0.9.0.
 */
private class NewApi(override val project: Project): ProtobufGradlePluginApi {

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

    override val generateProtoTasksAll: TaskCollection<GenerateProtoTask>
        get() {
            val getGenerateProtoTasks = extensionClass.getMethod("getGenerateProtoTasks")
            val generateProtoTaskCollection = getGenerateProtoTasks.invoke(extension)
            val generateProtoTaskCollectionClass = generateProtoTaskCollection.javaClass
            val all = generateProtoTaskCollectionClass.getMethod("all")
            val allCollection = all.invoke(generateProtoTaskCollection)
            @Suppress("UNCHECKED_CAST")
            return allCollection as TaskCollection<GenerateProtoTask>
        }

    override fun protoc(action: Action<ExecutableLocator>) {
        val setter = extensionClass.getMethod("protoc")
        setter.invoke(extension, action)
    }

    override fun plugins(action: Action<NamedDomainObjectContainer<ExecutableLocator>>) {
        val pluginsMethod = extensionClass.getMethod("plugins", Action::class.java)
        pluginsMethod.invoke(extension, action)
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
private class ConvApi(override val project: Project): ProtobufGradlePluginApi {

    @Suppress("DEPRECATION") /* Still have to use until migration to v0.9.1 is complete. */
    private val convention: Any = project.convention.getByName(conventionName)
    private val conventionsClass = convention.javaClass
    private val fld: Field = conventionsClass.getField(ConvApi.generatedFilesBaseDir)

    /**
     * The value of the `protobuf` field of the `ProtobufConvention` object.
     */
    private val protobufConfigurator: Any
    private val protobufConfiguratorClass: Class<*>

    init {
        val protobufField = conventionsClass.getField("protobuf")
        protobufConfigurator = protobufField.get(convention)
        protobufConfiguratorClass = protobufConfigurator.javaClass
    }

    override var generatedFilesBaseDir: String
        get() = fld.get(convention) as String
        set(value) = fld.set(convention, value)

    override val generateProtoTasksAll: TaskCollection<GenerateProtoTask>
        get() = project.tasks.withType(GenerateProtoTask::class.java)

    override fun protoc(action: Action<ExecutableLocator>) {
        val protocMethod = protobufConfiguratorClass.getMethod("protoc", Closure::class.java)
        val closure = closure<ExecutableLocator> { l -> action.execute(l) }
        protocMethod.invoke(protobufConfigurator, closure)
    }

    override fun plugins(action: Action<NamedDomainObjectContainer<ExecutableLocator>>) {
        val pluginsMethod = protobufConfiguratorClass.getMethod("plugins", Closure::class.java)
        val closure = closure<NamedDomainObjectContainer<ExecutableLocator>> { l ->
            action.execute(l)
        }
        pluginsMethod.invoke(protobufConfigurator, closure)
    }

    companion object {
        const val conventionName = "protobuf"
        const val generatedFilesBaseDir = "generatedFilesBaseDir"
    }
}
