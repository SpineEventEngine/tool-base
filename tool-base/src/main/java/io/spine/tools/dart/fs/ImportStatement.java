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

package io.spine.tools.dart.fs;

import com.google.errorprone.annotations.Immutable;
import io.spine.logging.WithLogging;
import io.spine.tools.code.Element;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.ExternalModules;
import io.spine.tools.fs.FileReference;
import org.checkerframework.checker.regex.qual.Regex;

import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;

/**
 * A source code line with an import statement.
 */
@Immutable
final class ImportStatement implements Element, WithLogging {

    @Regex(2)
    private static final Pattern PATTERN = compile("import [\"']([^:]+)[\"'] as (.+);");

    private final Path sourceDirectory;
    private final String text;
    private final String importPathAsDeclared;
    private final String importAlias;

    /**
     * Creates a new instance with the passed value.
     *
     * @param sourceFile
     *         the file containing the code line
     * @param text
     *         the text of the source code line with the import statement
     */
    static ImportStatement in(DartFile sourceFile, String text) {
        checkNotEmptyOrBlank(text);
        var sourceCodeDir = requireNonNull(sourceFile.parent());
        return new ImportStatement(sourceCodeDir, text);
    }

    private ImportStatement(Path sourceDirectory, String text) {
        this.sourceDirectory = sourceDirectory;
        this.text = text;
        var matcher = PATTERN.matcher(text);
        checkArgument(
                matcher.matches(),
                "The passed text is not recognized as an import statement (`%s`).", text
        );
        this.importPathAsDeclared = matcher.group(1);
        this.importAlias = matcher.group(2);
    }

    /**
     * Tells if the passed text is an import statement.
     */
    static boolean isDeclaredIn(String text) {
        checkNotNull(text);
        var matcher = PATTERN.matcher(text);
        return matcher.matches();
    }

    /**
     * If this line contains an import statement, converts it to a relative import statement
     * wherever possible.
     *
     * <p>If this line is not an import statement, returns it as is.
     *
     * <p>For import statements, converts an absolute path found after
     * {@code import} clause into a relative path, if one of the passed modules contains
     * such a file.
     *
     * <p>If none of the modules have such a file, returns the source code line as is.
     *
     * @param libPath
     *         the path to the {@code lib} project folder
     * @param modules
     *         the modules of the project to check the file referenced in
     *         the {@code import} statement
     */
    public ImportStatement resolve(Path libPath, ExternalModules modules) {
        var relativePath = importRelativeTo(libPath);
        var reference = FileReference.of(relativePath);
        for (var module : modules.asList()) {
            if (module.provides(reference)) {
                return resolve(module, relativePath);
            }
        }
        return this;
    }

    /**
     * Transforms the path found in the import statement to a path relative
     * to the file of this source line.
     */
    private Path importRelativeTo(Path libPath) {
        var debug = logger().atDebug();
        debug.log(() -> format("Import statement found in line: `%s`.", text));
        var absolutePath = sourceDirectory.resolve(importPathAsDeclared).normalize();
        debug.log(() -> format("Resolved against this file: `%s`.", absolutePath));
        var relativePath = libPath.relativize(absolutePath);
        debug.log(() -> format("Relative path: `%s`.", relativePath));
        return relativePath;
    }

    private ImportStatement resolve(ExternalModule module, Path relativePath) {
        var resolved = format(
                "import 'package:%s/%s' as %s;",
                module.name(), relativePath, importAlias
        );
        logger().atDebug().log(() -> format("Replacing with `%s`.", resolved));
        return new ImportStatement(sourceDirectory, resolved);
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImportStatement)) {
            return false;
        }
        var other = (ImportStatement) o;
        return text.equals(other.text) && sourceDirectory.equals(other.sourceDirectory);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
