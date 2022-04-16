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

package io.spine.tools.code.version

import java.io.OutputStream
import java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION
import java.util.jar.Attributes.Name.MANIFEST_VERSION
import java.util.jar.Manifest

/**
 * Allows to configure and write a manifest file.
 */
public class KManifestWriter: KManifest(Manifest()) {

    init {
        // The `Manifest-Version` version attribute must be initialized.
        // Otherwise, `java.util.jar.Attributes.writeMain()` skips writing its content.
        mainAttributes[MANIFEST_VERSION] = "1.0"
    }

    /**
     * Sets the [`Implementation-Version`][IMPLEMENTATION_VERSION] attribute of the manifest.
     *
     * @param value a non-empty version string
     */
    public fun implementationVersion(value: String) {
        require(value.isNotEmpty())
        mainAttributes[IMPLEMENTATION_VERSION] = value
    }

    /**
     * Writes the manifest to the given stream.
     */
    public fun write(stream: OutputStream) {
        impl.write(stream)
    }
}
