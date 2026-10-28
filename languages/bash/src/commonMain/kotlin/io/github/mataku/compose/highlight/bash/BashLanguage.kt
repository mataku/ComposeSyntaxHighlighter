package io.github.mataku.compose.highlight.bash

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.bash.internal.TreeSitterBash
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterBash.language()), HIGHLIGHTS_QUERY)
}

val Languages.Bash: Language get() = instance
