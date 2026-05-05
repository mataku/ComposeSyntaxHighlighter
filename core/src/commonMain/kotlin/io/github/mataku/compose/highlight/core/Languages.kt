package io.github.mataku.compose.highlight.core

/**
 * Cross-module catalog of bundled languages.
 *
 * Each `:languages:<name>` artifact contributes a `val Languages.<name>: Language` extension
 * forwarder pointing at the existing top-level `<Lang>Language` val (kept as the source of
 * truth). With at least one language module on the classpath, `Languages.<name>` becomes
 * IDE-discoverable via autocomplete on `Languages.`.
 *
 * Closed-enum semantics are intentionally not provided: cross-module enum extension is
 * impossible in Kotlin. To switch over a known set of languages, declare a consumer-side
 * enum and map its entries to `Languages.<name>`.
 */
object Languages
