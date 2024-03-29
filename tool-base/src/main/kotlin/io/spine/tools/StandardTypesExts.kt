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

@file:JvmName("StandardTypes")

package io.spine.tools

import io.spine.io.Files2
import io.spine.tools.fs.DirectoryName
import java.io.File
import java.nio.file.Path
import java.util.function.Supplier

/** Obtains a copy of this string with the first character capitalized . */
public fun String.titlecaseFirstChar(): String = replaceFirstChar(Char::titlecase)

/** Resolves an absolute file name obtained from the supplier. */
public fun Supplier<String>.toAbsoluteFile(): File = Files2.toAbsolute(get())

/** Adds relative name to this directory. */
public fun File.resolve(dir: DirectoryName): File = resolve(dir.value())

/** Adds relative directory to this directory. */
public operator fun File.div(dir: DirectoryName): File = resolve(dir)

/** Adds relative name to this directory. */
public fun Path.resolve(dir: DirectoryName): Path = resolve(dir.value())

/** Adds relative name to this directory. */
public operator fun Path.div(dir: DirectoryName): Path = resolve(dir)

/** Tells if this is a Protobuf source code file. */
public fun File.isProtoSource(): Boolean = extension == "proto"
