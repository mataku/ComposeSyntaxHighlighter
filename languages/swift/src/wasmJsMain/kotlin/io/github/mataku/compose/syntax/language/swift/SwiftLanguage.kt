package io.github.mataku.compose.syntax.language.swift

import io.github.mataku.compose.syntax.core.Language
import io.github.mataku.compose.syntax.core.webTreeSitterLanguage
import io.github.mataku.compose.syntax.language.swift.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
actual val SwiftLanguage: Language = webTreeSitterLanguage(
    key = "swift",
    highlightsQuery = HIGHLIGHTS_QUERY,
    grammarBytesProvider = { Res.readBytes("files/grammars/tree-sitter-swift.wasm") },
)
