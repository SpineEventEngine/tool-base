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

package io.spine.tools.dart.fs

import io.kotest.matchers.collections.shouldContainExactly
import io.spine.tools.fs.DirectoryPattern
import io.spine.tools.fs.ExternalModule
import io.spine.tools.fs.ExternalModules
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readLines
import kotlin.io.path.writeText
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`DartFile` should")
internal class DartFileSpec {

    @Test
    fun `rewrite import lines and leave other lines intact`(@TempDir libPath: Path) {
        val sub = libPath.resolve("sub")
        sub.createDirectories()
        val path = sub.resolve("file.dart")
        val importLine = "import '../foo/bar.dart' as b;"
        path.writeText(
            """
            $importLine
            void main() {}
            """.trimIndent()
        )
        val file = DartFile.read(path)
        // None of the modules provide the referenced file, so the import line is preserved
        // verbatim while non-import lines are copied through unchanged.
        val modules = ExternalModules(
            ExternalModule("other", DirectoryPattern.listOf("unrelated"))
        )

        file.resolveImports(libPath, modules)

        path.readLines() shouldContainExactly listOf(importLine, "void main() {}")
    }

    @Test
    fun `rewrite an import to the 'package' form when a module provides the file`(
        @TempDir libPath: Path
    ) {
        val sub = libPath.resolve("sub")
        sub.createDirectories()
        val path = sub.resolve("file.dart")
        path.writeText(
            """
            import '../foo/bar.dart' as b;
            void main() {}
            """.trimIndent()
        )
        val file = DartFile.read(path)
        val modules = ExternalModules(
            ExternalModule("mymod", DirectoryPattern.listOf("foo"))
        )

        file.resolveImports(libPath, modules)

        path.readLines() shouldContainExactly listOf(
            "import 'package:mymod/foo/bar.dart' as b;",
            "void main() {}"
        )
    }
}
