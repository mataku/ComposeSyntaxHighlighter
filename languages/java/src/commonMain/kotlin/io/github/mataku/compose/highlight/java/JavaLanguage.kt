package io.github.mataku.compose.highlight.java

import io.github.mataku.compose.highlight.core.Language
import io.github.mataku.compose.highlight.core.Languages
import io.github.mataku.compose.highlight.core.kTreeSitterLanguage
import io.github.mataku.compose.highlight.java.internal.TreeSitterJava
import io.github.treesitter.ktreesitter.Language as TsLanguage

val JavaLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterJava.language()), HIGHLIGHTS_QUERY)
}

val Languages.java: Language get() = JavaLanguage
