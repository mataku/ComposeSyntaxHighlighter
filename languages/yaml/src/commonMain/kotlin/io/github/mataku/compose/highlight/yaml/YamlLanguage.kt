package io.github.mataku.compose.highlight.yaml

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.yaml.internal.TreeSitterYaml
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterYaml.language()), HIGHLIGHTS_QUERY)
}

val Languages.Yaml: Language get() = instance
