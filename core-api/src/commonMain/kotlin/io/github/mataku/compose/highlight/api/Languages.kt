package io.github.mataku.compose.highlight.api

/**
 * Cross-module catalog of bundled languages.
 *
 * Each `:languages:<name>` artifact contributes a `val Languages.<Name>: Language` extension
 * property (e.g. `Languages.Kotlin`, `Languages.Swift`). With at least one language module on
 * the classpath, the entries become IDE-discoverable via autocomplete on `Languages.`.
 *
 * Closed-enum semantics are intentionally not provided: cross-module enum extension is
 * impossible in Kotlin. To switch over a known set of languages, declare a consumer-side
 * enum and map its entries to `Languages.<Name>`.
 */
object Languages
