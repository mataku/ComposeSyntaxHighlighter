package io.github.mataku.compose.syntax.core

import kotlin.js.Promise

internal external interface WtsTree : JsAny {
    val rootNode: WtsSyntaxNode
}

internal external interface WtsSyntaxNode : JsAny {
    val startIndex: Int
    val endIndex: Int
}

internal external interface WtsQueryCapture : JsAny {
    val name: String
    val node: WtsSyntaxNode
}

internal external interface WtsQuery : JsAny

internal external interface WtsLanguage : JsAny

internal external interface WtsParser : JsAny

internal external interface WtsCaptureArray : JsAny {
    val length: Int
}

internal external interface WtsModule : JsAny

internal fun wtsImportModule(): Promise<WtsModule> =
    js("import('web-tree-sitter')")

internal fun wtsParserInit(module: WtsModule): Promise<JsAny?> =
    js("module.default.init()")

internal fun wtsLanguageLoad(module: WtsModule, bytes: JsAny): Promise<WtsLanguage> =
    js("module.default.Language.load(bytes)")

internal fun wtsLanguageQuery(language: WtsLanguage, source: String): WtsQuery =
    js("language.query(source)")

internal fun wtsCreateParser(module: WtsModule): WtsParser =
    js("new module.default()")

internal fun wtsParserSetLanguage(parser: WtsParser, language: WtsLanguage) {
    js("parser.setLanguage(language)")
}

internal fun wtsParserParse(parser: WtsParser, source: String): WtsTree =
    js("parser.parse(source)")

internal fun wtsTreeRootNode(tree: WtsTree): WtsSyntaxNode =
    js("tree.rootNode")

internal fun wtsQueryCaptures(query: WtsQuery, root: WtsSyntaxNode): WtsCaptureArray =
    js("query.captures(root)")

internal fun wtsCaptureAt(captures: WtsCaptureArray, index: Int): WtsQueryCapture =
    js("captures[index]")

internal fun wtsParserDelete(parser: WtsParser) {
    js("parser.delete()")
}

internal fun wtsTreeDelete(tree: WtsTree) {
    js("tree.delete()")
}

internal fun wtsQueryDelete(query: WtsQuery) {
    js("query.delete()")
}

internal fun wtsCreateUint8Array(size: Int): JsAny = js("new Uint8Array(size)")

internal fun wtsSetUint8At(buffer: JsAny, index: Int, value: Int) {
    js("buffer[index] = value")
}

internal fun ByteArray.toUint8Array(): JsAny {
    val buffer = wtsCreateUint8Array(size)
    for (i in indices) {
        wtsSetUint8At(buffer, i, this[i].toInt() and 0xFF)
    }
    return buffer
}

internal fun wtsLogError(message: String, error: Throwable) {
    consoleErrorJs(message, error.toString())
}

private fun consoleErrorJs(message: String, error: String) {
    js("console.error(message, error)")
}
