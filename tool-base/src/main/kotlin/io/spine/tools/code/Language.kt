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

package io.spine.tools.code

import io.spine.io.Glob
import java.io.File
import java.nio.file.Path

/**
 * A programming language or a specific syntax.
 *
 * For example, `"Java"`, `"Python 3.x"`, etc.
 */
public abstract class Language internal constructor(

    /**
     * Label to distinguish the language.
     */
    public val name: String,

    /**
     * Extensions of source code files on this language with or without the leading dot.
     *
     * Some languages may have more than one type of files, e.g. header files ".h" for C source
     * code files (".c"). Also, conventions for some languages allow alternative extensions,
     * e.g. ".cc" and ".cpp" for C++.
     *
     * The passed values are converted to lower case.
     */
    fileExtensions: Iterable<String>,

    /**
     * Patterns one of which a language source file must match.
     *
     * If not specified, patterns matching [fileExtensions] will be created.
     */
    filePattern: Glob? = null
) {

    /**
     * The extension of source code files in this language without the leading dot.
     */
    public val fileExtensions: List<String>

    /**
     * Pattern which source code files must match.
     */
    private val filePattern: Glob

    init {
        this.fileExtensions = fileExtensions
            .map { it.replaceFirst(".", "") }
            .map { it.lowercase() }
            .toMutableList()
            .sorted()
        this.filePattern = filePattern ?: Glob.extension(this.fileExtensions)
    }

    /**
     * Creates a syntactically valid one-line comment in this language.
     *
     * @param line
     *          the contents of the comment
     * @return a line which can be safely inserted into a code file.
     */
    public abstract fun comment(line: String): String

    /**
     * Checks if the given file is a code file in this language.
     *
     * If the passed instance is a directory, rather than a file, returns `false`.
     */
    public fun matches(file: File): Boolean = matches(file.toPath())

    /**
     * Checks if the given file is a code file in this language.
     *
     * If the passed instance is a directory, rather than a file, returns `false`.
     */
    public fun matches(file: Path): Boolean = filePattern.matches(file)

    /**
     * Returns the name of the language.
     */
    override fun toString(): String = name
}

/**
 * A C-like language.
 *
 * Supports double-slash comments (`// <comment body>`).
 */
@Deprecated("Use `SlashAsteriskCommentLang` instead.")
public class SlashCommentLanguage(
    name: String,
    fileExtensions: Iterable<String>
) : Language(name, fileExtensions) {

    override fun comment(line: String): String = "// $line"
}

/**
 * A C-like language.
 *
 * Supports slash-asterisk-asterisk-slash comments (`/* <comment body> */`).
 */
public class SlashAsteriskCommentLang(
    name: String,
    fileExtensions: Iterable<String>
) : Language(name, fileExtensions) {

    override fun comment(line: String): String = "/* $line */"
}

/**
 * A collection of commonly used [Language]s.
 *
 * If this prepared set is not enough, users are encouraged to create custom [Language] types
 * by either extending the class directly, or using one of its existing subtypes, such as
 * [SlashAsteriskCommentLang].
 */
public object CommonLanguages {

    /**
     * Any language will do.
     *
     * This instance indicates that any programming language can be accepted.
     *
     * Intended to be used for filtering source files by language via file name conventions.
     * If no filtering required, but a [Language] is needed, use `CommonLanguages.any`.
     *
     * Does not support [comments][Language.comment].
     */
    @get:JvmName("any")
    @JvmStatic
    public val any: Language = object : Language("any language", listOf(""), Glob.any) {
        override fun comment(line: String): String {
            throw UnsupportedOperationException("`$name` does not support comments.")
        }
    }

    @get:JvmName("kotlin")
    @JvmStatic
    public val Kotlin: Language = SlashAsteriskCommentLang("Kotlin", listOf("kt"))

    @get:JvmName("java")
    @JvmStatic
    public val Java: Language = SlashAsteriskCommentLang("Java", listOf("java"))

    @get:JvmName("javaScript")
    @JvmStatic
    public val JavaScript: Language = SlashAsteriskCommentLang("JavaScript", listOf("js"))
}
