package io.github.mataku.compose.highlight.java

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.java.internal.TreeSitterJava
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterJava.language()), HIGHLIGHTS_QUERY)
}

val Languages.Java: Language get() = instance
