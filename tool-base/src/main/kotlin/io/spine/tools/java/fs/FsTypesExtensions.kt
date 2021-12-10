/*
 * Copyright 2021, TeamDev. All rights reserved.
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

@file:JvmName("JavaFiles")

package io.spine.tools.java.fs

import io.spine.code.fs.SourceCodeDirectory
import io.spine.code.java.PackageName
import io.spine.code.java.PackageName.delimiterChar
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Converts this package name to a relative directory path.
 */
public fun PackageName.toDirectory(): Path {
    val packagePath: String = value().replace(delimiterChar(), File.separatorChar)
    val path = Paths.get(packagePath)
    return path
}

/**
 * Obtains the source file with the given name.
 */
public fun Path.resolve(file: FileName): SourceFile {
    val filePath: Path = resolve(file.value())
    return SourceFile.of(filePath)
}

/**
 * Obtains the source code file for the passed name.
 */
public fun SourceCodeDirectory.resolve(file: FileName): SourceFile = path().resolve(file)

/**
 * Obtains the source code path for the passed file.
 */
public fun SourceCodeDirectory.resolve(file: Path): Path = path().resolve(file)
