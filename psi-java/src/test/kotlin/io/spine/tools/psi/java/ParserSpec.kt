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

package io.spine.tools.psi.java

import com.intellij.psi.PsiJavaFile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.spine.string.Separator
import io.spine.string.Separator.CR
import io.spine.string.Separator.CRLF
import io.spine.string.Separator.LF
import io.spine.tools.psi.readResource
import java.io.File
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`Parser` should")
class ParserSpec : PsiTest() {

    companion object {

        lateinit var parser: Parser
        lateinit var code: String

        @JvmStatic
        @BeforeAll
        fun setupIdea() {
            Environment.setUp()
            parser = Parser(Environment.project)
            code = readResource("FileOnDisk.java")
        }
    }

    @Nested inner class
    `parse loaded Java code` {

        @Test
        fun `providing synthetic file name`() {
            val psiJavaFile = parser.parse(code)
            psiJavaFile.name shouldStartWith "__to_parse_"
        }

        @Test
        fun `using passed file reference`() {
            val file = File("path/to/file.java")
            val psiJavaFile: PsiJavaFile = parser.parse(code, file)

            psiJavaFile.name shouldContain file.toString()
        }
    }

    @Nested inner class
    `accept code with` {

        @Test
        fun `'LF' line separators`() {
            parser.parse(code).text shouldBe code
        }

        @Test
        fun `'CRLF' line separators`() {
            assertParsedAsLf(CRLF)
        }

        @Test
        fun `'CR' line separators`() {
            assertParsedAsLf(CR)
        }

        @Test
        fun `mixed line separators`() {
            assertParsedAsLf(LF, CR, CRLF)
        }

        /**
         * Asserts that [code] with its line separators replaced by the given ones
         * is parsed into a file with the same text as [code].
         *
         * The [separators] must include `CR` or `CRLF`, so that the parsed code
         * differs from [code].
         */
        private fun assertParsedAsLf(vararg separators: Separator) {
            val source = code.withSeparators(*separators)
            source shouldContain CR.value
            parser.parse(source).text shouldBe code
        }
    }
}

/**
 * Replaces the `LF` line separators of this text with the given ones,
 * cycling through them.
 *
 * The [separators] must not have `LF` right after `CR`. Otherwise, around
 * an empty line, the two would merge into one `CRLF`.
 */
private fun String.withSeparators(vararg separators: Separator): String {
    require(separators.isNotEmpty()) { "At least one separator is required." }
    val lines = split(LF.value)
    return buildString {
        lines.forEachIndexed { index, line ->
            append(line)
            if (index < lines.lastIndex) {
                append(separators[index % separators.size].value)
            }
        }
    }
}
