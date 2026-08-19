/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.tools.archive;

import io.spine.annotation.Internal;

import java.io.File;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

/**
 * The enumeration of acknowledged archive file extensions.
 */
@Internal
enum ArchiveExtension {

    ZIP("zip"),
    JAR("jar");

    private final Pattern pattern;

    ArchiveExtension(String extensionName) {
        var regex = format("^.*\\.%s$", extensionName);
        this.pattern = compile(regex, CASE_INSENSITIVE);
    }

    /**
     * Matches the given file to the extensions.
     *
     * @return {@code true} if the file is a ZIP archive, {@code false} otherwise
     */
    static boolean anyMatch(File file) {
        var matchingExtension = Stream.of(values())
                                      .filter(ext -> ext.matchesFile(file))
                                      .findAny();
        return matchingExtension.isPresent();
    }

    private boolean matchesFile(File file) {
        var name = file.getName();
        var matcher = pattern.matcher(name);
        var matches = matcher.matches();
        return matches;
    }
}
