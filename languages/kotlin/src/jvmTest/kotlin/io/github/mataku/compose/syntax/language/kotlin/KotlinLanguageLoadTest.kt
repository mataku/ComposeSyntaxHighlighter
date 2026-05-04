package io.github.mataku.compose.syntax.language.kotlin

import kotlin.test.Test
import kotlin.test.assertNotNull

class KotlinLanguageLoadTest {
    @Test
    fun loads_native_library_and_constructs_language() {
        val language = KotlinLanguage
        assertNotNull(language)
    }
}
