package com.mataku.composesyntaxhighlighter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.mataku.compose.highlight.core.LocalSyntaxTheme
import io.github.mataku.compose.highlight.core.SyntaxHighlightedText
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.kotlin.KotlinLanguage
import io.github.mataku.compose.highlight.swift.SwiftLanguage

private enum class DemoLanguage(val label: String) {
    Kotlin("Kotlin"),
    Swift("Swift"),
}

private enum class DemoTheme(val label: String) {
    Dark("Dark"),
    Light("Light"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlighterDemo() {
    var selectedLang by remember { mutableStateOf(DemoLanguage.Kotlin) }
    var selectedTheme by remember { mutableStateOf(DemoTheme.Dark) }

    val (code, language) = when (selectedLang) {
        DemoLanguage.Kotlin -> SampleCode.kotlin to KotlinLanguage
        DemoLanguage.Swift -> SampleCode.swift to SwiftLanguage
    }
    val theme = when (selectedTheme) {
        DemoTheme.Dark -> SyntaxTheme.darkDefault()
        DemoTheme.Light -> SyntaxTheme.lightDefault()
    }
    val surfaceColor = if (selectedTheme == DemoTheme.Dark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)

    MaterialTheme {
        CompositionLocalProvider(LocalSyntaxTheme provides theme) {
            Scaffold(
                topBar = {
                    TopAppBar(title = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DemoLanguage.entries.forEach { l ->
                                FilterChip(
                                    selected = l == selectedLang,
                                    onClick = { selectedLang = l },
                                    label = { Text(l.label) },
                                )
                            }
                            DemoTheme.entries.forEach { t ->
                                FilterChip(
                                    selected = t == selectedTheme,
                                    onClick = { selectedTheme = t },
                                    label = { Text(t.label) },
                                )
                            }
                        }
                    })
                },
            ) { padding ->
                Surface(color = surfaceColor, modifier = Modifier.fillMaxSize().padding(padding)) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    ) {
                        SyntaxHighlightedText(code = code, language = language)
                    }
                }
            }
        }
    }
}
