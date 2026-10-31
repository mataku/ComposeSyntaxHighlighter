package io.github.mataku.compose.highlight.json

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.json.internal.TreeSitterJson
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterJson.language()), HIGHLIGHTS_QUERY)
}

val Languages.Json: Language get() = instance
