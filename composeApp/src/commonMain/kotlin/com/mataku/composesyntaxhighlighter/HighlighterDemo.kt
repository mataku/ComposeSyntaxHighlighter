package com.mataku.composesyntaxhighlighter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mataku.compose.highlight.core.Languages
import io.github.mataku.compose.highlight.core.SyntaxHighlightedText
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.go.go
import io.github.mataku.compose.highlight.java.java
import io.github.mataku.compose.highlight.kotlin.kotlin
import io.github.mataku.compose.highlight.python.python
import io.github.mataku.compose.highlight.ruby.ruby
import io.github.mataku.compose.highlight.rust.rust
import io.github.mataku.compose.highlight.swift.swift

private enum class DemoLanguage(val label: String) {
  Kotlin("Kotlin"),
  Swift("Swift"),
  Ruby("Ruby"),
  Rust("Rust"),
  Python("Python"),
  Go("Go"),
  Java("Java"),
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

  val (code, language) = when (selectedLang) {
    DemoLanguage.Kotlin -> SampleCode.kotlin to Languages.kotlin
    DemoLanguage.Swift -> SampleCode.swift to Languages.swift
    DemoLanguage.Ruby -> SampleCode.ruby to Languages.ruby
    DemoLanguage.Rust -> SampleCode.rust to Languages.rust
    DemoLanguage.Python -> SampleCode.python to Languages.python
    DemoLanguage.Go -> SampleCode.go to Languages.go
    DemoLanguage.Java -> SampleCode.java to Languages.java
  }
  val theme = when (selectedTheme) {
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

  MaterialTheme {
    Scaffold(
      topBar = {
        TopAppBar(title = { Text("Compose Highlight Demo") })
      },
    ) { padding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          DemoDropdown(
            label = "Language",
            options = DemoLanguage.entries,
            selected = selectedLang,
            optionLabel = { it.label },
            onSelect = { selectedLang = it },
            modifier = Modifier.weight(1f),
          )
          DemoDropdown(
            label = "Theme",
            options = DemoTheme.entries,
            selected = selectedTheme,
            optionLabel = { it.label },
            onSelect = { selectedTheme = it },
            modifier = Modifier.weight(1f),
          )
        }
        Surface(color = theme.background ?: MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
          Column(modifier = Modifier.padding(16.dp)) {
            SyntaxHighlightedText(code = code, language = language, theme = theme)
          }
        }
      }
    }
  }
}


@Composable
private fun <T> DemoDropdown(
  label: String,
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
        text = "$label: ${optionLabel(selected)} ▾",
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
