package io.github.mataku.compose.highlight.xml

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.xml.internal.TreeSitterXml
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterXml.language()), HIGHLIGHTS_QUERY)
}

val Languages.Xml: Language get() = instance
