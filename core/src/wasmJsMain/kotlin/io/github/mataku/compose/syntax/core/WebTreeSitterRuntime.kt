package io.github.mataku.compose.syntax.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.await

internal object WebTreeSitterRuntime {
    private var moduleDeferred: CompletableDeferred<WtsModule>? = null
    private val grammarCache = mutableMapOf<String, CompletableDeferred<WtsLanguage>>()

    suspend fun module(): WtsModule {
        moduleDeferred?.let { return it.await() }
        val pending = CompletableDeferred<WtsModule>()
        moduleDeferred = pending
        try {
            val module = wtsImportModule().await<WtsModule>()
            wtsParserInit(module).await<JsAny?>()
            pending.complete(module)
            return module
        } catch (t: Throwable) {
            pending.completeExceptionally(t)
            moduleDeferred = null
            throw t
        }
    }

    suspend fun loadGrammar(key: String, bytesProvider: suspend () -> ByteArray): WtsLanguage {
        grammarCache[key]?.let { return it.await() }
        val pending = CompletableDeferred<WtsLanguage>()
        grammarCache[key] = pending
        try {
            val module = module()
            val bytes = bytesProvider()
            val u8 = bytes.toUint8Array()
            val language = wtsLanguageLoad(module, u8).await<WtsLanguage>()
            pending.complete(language)
            return language
        } catch (t: Throwable) {
            pending.completeExceptionally(t)
            grammarCache.remove(key)
            throw t
        }
    }
}
