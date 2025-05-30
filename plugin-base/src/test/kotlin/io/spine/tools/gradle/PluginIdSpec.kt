package io.spine.tools.gradle

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`PluginId` should")
internal class PluginIdSpec {

    @Test
    fun `accept valid values`() {
        val validIds = listOf(
            "com.example.plugin",
            "net.company.tool",
            "a.b",
            "abc.def-ghi"
        )
        validIds.forEach { id ->
            PluginId.isValid(id) shouldBe true
        }
    }

    @Test
    fun `fail on invalid values`() {
        val invalidIds = listOf(
            "1com.example.plugin",       // starts with a digit
            "-com.example.plugin",       // starts with dash
            ".com.example.plugin",       // starts with a dot
            "com.example.plugin.",       // ends with a dot
            "com..example.plugin",       // double dots
            "com.gradle.plugin",         // forbidden namespace
            "org.gradle.tool",           // forbidden namespace
            "com.gradleware.tool",       // forbidden namespace
            "com.example.PLUGIN",        // uppercase
            "com.Example.plugin",        // uppercase in namespace
            "com.example.plugin!",       // invalid char
            "plugin"                     // no dot
        )

        invalidIds.forEach { id ->
            withClue("The ID `$id` should be invalid.") {
                PluginId.isValid(id) shouldBe false
            }
            assertThrows<IllegalArgumentException> { PluginId(id) }
        }
    }

    @Test
    fun `use its value in the string representation`() {
        val id = "com.example.plugin"
        PluginId(id).run {
            toString() shouldBe id
            value shouldBe id
        }
    }
}
