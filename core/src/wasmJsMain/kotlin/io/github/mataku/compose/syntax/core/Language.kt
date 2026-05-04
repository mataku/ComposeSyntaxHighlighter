package io.github.mataku.compose.syntax.core

actual class Language internal constructor(
    internal val key: String,
    internal val highlightsQuery: String,
    internal val grammarBytesProvider: suspend () -> ByteArray,
)

fun webTreeSitterLanguage(
    key: String,
    highlightsQuery: String,
    grammarBytesProvider: suspend () -> ByteArray,
): Language = Language(key, highlightsQuery, grammarBytesProvider)
