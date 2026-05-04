package io.github.mataku.compose.syntax.language.kotlin

import io.github.mataku.compose.syntax.core.Language
import io.github.mataku.compose.syntax.core.webTreeSitterLanguage
import io.github.mataku.compose.syntax.language.kotlin.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
actual val KotlinLanguage: Language = webTreeSitterLanguage(
    key = "kotlin",
    highlightsQuery = HIGHLIGHTS_QUERY,
    grammarBytesProvider = { Res.readBytes("files/grammars/tree-sitter-kotlin.wasm") },
)
