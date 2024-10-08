/*
 * Copyright 2024, TeamDev. All rights reserved.
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

package io.spine.tools.code

import io.spine.io.Glob
import java.io.File
import java.nio.file.Path
import kotlin.io.path.isDirectory

/**
 * A programming language or a specific syntax.
 *
 * For example, `"Java"`, `"Python 3.x"`, etc.
 *
 * This abstract class is supposed to be extended by an `object` to make it
 * a singleton and a typed [Language] instance.
 *
 * @see Protobuf
 * @see Java
 * @see Kotlin
 * @see JavaScript
 * @see TypeScript
 */
public abstract class Language
protected constructor(

    /**
     * Label to distinguish the language.
     */
    public val name: String,

    /**
     * Extensions of source code files on this language with or without the leading dot.
     *
     * Some languages may have more than one type of files, e.g., header files ".h" for C source
     * code files (".c"). Also, conventions for some languages allow alternative extensions,
     * e.g. ".cc" and ".cpp" for C++.
     *
     * The passed values are converted to the lower case.
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
    public val fileExtensions: List<String> = fileExtensions
        .map { it.replaceFirst(".", "") }
        .map { it.lowercase() }
        .toMutableList()
        .sorted()

    /**
     * Pattern which source code files must match.
     */
    private val filePattern: Glob = filePattern ?: Glob.extension(this.fileExtensions)

    /**
     * Creates a syntactically valid one-line comment in this language.
     *
     * @param line The contents of the comment.
     * @return A line which can be safely inserted into a code file.
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

    public companion object {

        /**
         * The list of known languages.
         */
        private val languages: List<Language> by lazy {
            listOf(
                Java,
                Kotlin,
                JavaScript,
                TypeScript,
                Dart,
                Protobuf,
                AnyLanguage
            )
        }

        /**
         * Obtains a language of the give file taking its extension.
         *
         * @return One of the supported languages, or [AnyLanguage] if the extension is
         *  absent or not known.
         *
         * @throws IllegalArgumentException If the given instance represents a directory.
         */
        @JvmStatic
        public fun of(file: Path): Language {
            require(!file.isDirectory()) {
                "The path `$file` is a directory, not a file."
            }
            return languages.first { it.matches(file) }
        }

        /**
         * Obtains a language of the give file taking its extension.
         *
         * @return One of the supported languages, or [AnyLanguage] if the extension is
         *   absent or not known.
         *
         * @throws IllegalArgumentException If the given instance represents a directory.
         */
        @JvmStatic
        public fun of(file: File): Language = of(file.toPath())
    }
}

/**
 * A C-like language.
 *
 * Supports slash-asterisk-asterisk-slash comments (`/* <comment body> */`).
 */
public abstract class SlashAsteriskCommentLang(
    name: String,
    fileExtensions: Iterable<String>
) : Language(name, fileExtensions) {

    override fun comment(line: String): String = "/* $line */"
}

/**
 * This object indicates that any programming language can be accepted.
 *
 * Intended to be used for filtering source files by language via file name conventions.
 * If no filtering required, but a [Language] is needed, use [AnyLanguage].
 *
 * Does not support [comments][Language.comment].
 */
public object AnyLanguage : Language("any language", listOf(""), Glob.any) {
    override fun comment(line: String): String {
        throw UnsupportedOperationException("`$name` does not support comments.")
    }

    /**
     * Returns the typed instance of this language for usage in Java code.
     */
    @JvmStatic
    public fun willDo(): AnyLanguage = this
}

/**
 * A typed [Language] for Protocol Buffers.
 */
public object Protobuf : SlashAsteriskCommentLang("Protobuf", listOf("proto")) {

    /**
     * Returns the typed instance of this language for usage in Java code.
     */
    @JvmStatic
    public fun lang(): Protobuf = this
}

/**
 * A typed [Language] for Java.
 */
@Suppress("ACCIDENTAL_OVERRIDE")
public object Java : SlashAsteriskCommentLang("Java", listOf("java")) {

    /**
     * Returns the typed instance of this language for usage in Java code.
     */
    @JvmStatic
    public fun lang(): Java = this
}

/**
 * A typed [Language] for Kotlin.
 */
public object Kotlin: SlashAsteriskCommentLang("Kotlin", listOf("kt")) {

    /**
     * Returns the typed instance of this language for usage in Java code.
     */
    @JvmStatic
    public fun lang(): Kotlin = this
}

/**
 * A typed [Language] for JavaScript.
 */
public object JavaScript : SlashAsteriskCommentLang("JavaScript", listOf("js")) {

    /**
     * Returns the typed instance of this language for usage in Java code.
     */
    @JvmStatic
    public fun lang(): JavaScript = this
}

/**
 * A typed [Language] for TypeScript.
 *
 * @see <a href="https://stackoverflow.com/questions/37063569/typescript-various-file-extensions-explained">
 *     TypeScript file extensions explained</a>
 */
public object TypeScript :
    SlashAsteriskCommentLang("TypeScript", listOf("ts", "d.ts", "js", "map")) {

    /**
    * Returns the typed instance of this language for usage in Java code.
    */
    @JvmStatic
    public fun lang(): TypeScript = this
}

/**
 * A typed [Language] for Dart.
 */
public object Dart :
    SlashAsteriskCommentLang("Dart", listOf("dart")) {

    /**
     * Returns the typed instance of this language for usage in Java code.
     */
    @JvmStatic
    public fun lang(): Dart = this
}
