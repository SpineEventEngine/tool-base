/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package io.spine.tools.psi

import com.intellij.openapi.util.text.StringUtilRt

/**
 * Converts the line separators in the string to the one used by PSI (`"\n"`).
 *
 * If the string has no line separators other than `"\n"`, returns this string
 * without copying it. So, the calling code may convert its input without checking it first.
 */
public fun String.convertLineSeparators(): String =
    StringUtilRt.convertLineSeparators(this)
