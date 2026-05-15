package com.mataku.composesyntaxhighlighter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.go.Go
import io.github.mataku.compose.highlight.java.Java
import io.github.mataku.compose.highlight.javascript.Javascript
import io.github.mataku.compose.highlight.kotlin.Kotlin
import io.github.mataku.compose.highlight.markdown.Markdown
import io.github.mataku.compose.highlight.material3.SyntaxHighlightedText
import io.github.mataku.compose.highlight.python.Python
import io.github.mataku.compose.highlight.ruby.Ruby
import io.github.mataku.compose.highlight.rust.Rust
import io.github.mataku.compose.highlight.swift.Swift
import io.github.mataku.compose.highlight.tsx.Tsx
import io.github.mataku.compose.highlight.typescript.Typescript

private enum class DemoLanguage(val label: String) {
  Kotlin("Kotlin"),
  Swift("Swift"),
  Ruby("Ruby"),
  Rust("Rust"),
  Python("Python"),
  Go("Go"),
  Java("Java"),
  Javascript("JavaScript"),
  Typescript("TypeScript"),
  Tsx("TSX"),
  Markdown("Markdown"),
}

private enum class DemoTheme(val label: String) {
  DefaultDark("Default Dark"),
  DefaultLight("Default Light"),
  SolarizedDark("Solarized Dark"),
  SolarizedLight("Solarized Light"),
  GitHubDark("GitHub Dark"),
  GitHubLight("GitHub Light"),
  OneDark("One Dark"),
  OneLight("One Light"),
  Dracula("Dracula"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlighterDemo() {
  var selectedLang by remember { mutableStateOf(DemoLanguage.Kotlin) }
  var selectedTheme by remember { mutableStateOf(DemoTheme.DefaultDark) }
  var showEditor by remember { mutableStateOf(false) }
  if (showEditor) {
    EditorDemo(onBack = { showEditor = false })
    return
  }

  val (code, language) = when (selectedLang) {
    DemoLanguage.Kotlin -> SampleCode.kotlin to Languages.Kotlin
    DemoLanguage.Swift -> SampleCode.swift to Languages.Swift
    DemoLanguage.Ruby -> SampleCode.ruby to Languages.Ruby
    DemoLanguage.Rust -> SampleCode.rust to Languages.Rust
    DemoLanguage.Python -> SampleCode.python to Languages.Python
    DemoLanguage.Go -> SampleCode.go to Languages.Go
    DemoLanguage.Java -> SampleCode.java to Languages.Java
    DemoLanguage.Javascript -> SampleCode.javascript to Languages.Javascript
    DemoLanguage.Typescript -> SampleCode.typescript to Languages.Typescript
    DemoLanguage.Tsx -> SampleCode.tsx to Languages.Tsx
    DemoLanguage.Markdown -> SampleCode.markdown to Languages.Markdown
  }
  val baseTheme = when (selectedTheme) {
    DemoTheme.DefaultDark -> SyntaxTheme.DarkDefault
    DemoTheme.DefaultLight -> SyntaxTheme.LightDefault
    DemoTheme.SolarizedDark -> SyntaxTheme.SolarizedDark
    DemoTheme.SolarizedLight -> SyntaxTheme.SolarizedLight
    DemoTheme.GitHubDark -> SyntaxTheme.GitHubDark
    DemoTheme.GitHubLight -> SyntaxTheme.GitHubLight
    DemoTheme.OneDark -> SyntaxTheme.OneDark
    DemoTheme.OneLight -> SyntaxTheme.OneLight
    DemoTheme.Dracula -> SyntaxTheme.Dracula
  }
  val theme = if (selectedLang == DemoLanguage.Markdown) {
    baseTheme.copy(
      extras = baseTheme.extras + mapOf(
        "text.title" to SpanStyle(
          fontWeight = FontWeight.Bold,
          color = baseTheme.keyword?.color ?: baseTheme.baseStyle.color,
        ),
        "text.emphasis" to SpanStyle(fontStyle = FontStyle.Italic),
        "text.strong" to SpanStyle(fontWeight = FontWeight.Bold),
        "text.literal" to SpanStyle(
          fontFamily = FontFamily.Monospace,
          color = baseTheme.string?.color ?: baseTheme.baseStyle.color,
        ),
        "text.uri" to SpanStyle(
          color = baseTheme.function?.color ?: baseTheme.baseStyle.color,
          textDecoration = TextDecoration.Underline,
        ),
        "text.reference" to SpanStyle(
          color = baseTheme.property?.color ?: baseTheme.baseStyle.color,
        ),
      ),
    )
  } else {
    baseTheme
  }

  MaterialTheme {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text("Compose Syntax Highlight Demo") },
          actions = {
            TextButton(onClick = { showEditor = true }) {
              Text("Editor")
            }
          },
          modifier = Modifier.shadow(4.dp),
        )
      },
    ) { padding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .verticalScroll(rememberScrollState()),
      ) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceContainer,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .height(48.dp)
              .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            DemoDropdown(
              options = DemoLanguage.entries,
              selected = selectedLang,
              optionLabel = { it.label },
              onSelect = { selectedLang = it },
              modifier = Modifier.weight(1f),
            )
            DemoDropdown(
              options = DemoTheme.entries,
              selected = selectedTheme,
              optionLabel = { it.label },
              onSelect = { selectedTheme = it },
              modifier = Modifier.weight(1f),
            )
          }
        }
        SyntaxHighlightedText(
          code = code,
          language = language,
          theme = theme,
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(16.dp),
        )
      }
    }
  }
}

@Composable
private fun <T> DemoDropdown(
  options: List<T>,
  selected: T,
  optionLabel: (T) -> String,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  Box(modifier) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { expanded = true }
        .padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "${optionLabel(selected)} ▾",
        style = MaterialTheme.typography.bodyLarge,
      )
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(optionLabel(option)) },
          onClick = {
            onSelect(option)
            expanded = false
          },
        )
      }
    }
  }
}
