package com.mataku.composesyntaxhighlighter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.kotlin.Kotlin
import io.github.mataku.compose.highlight.material3.textfield.SyntaxHighlightedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorDemo(onBack: () -> Unit = {}) {
  val state = remember { TextFieldState(initialText = SampleCode.kotlin) }
  MaterialTheme {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text("Editor Demo (Kotlin)") },
          actions = {
            TextButton(onClick = onBack) {
              Text("Viewer")
            }
          },
          modifier = Modifier.shadow(4.dp),
        )
      },
    ) { padding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
      ) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceContainer,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "Type to edit. Highlighting updates incrementally.",
            modifier = Modifier
              .height(48.dp)
              .padding(horizontal = 16.dp)
              .wrapContentHeight(),
          )
        }
        SyntaxHighlightedTextField(
          state = state,
          language = Languages.Kotlin,
          theme = SyntaxTheme.DarkDefault,
          modifier = Modifier
            .fillMaxSize()
            .background(SyntaxTheme.DarkDefault.background ?: MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        )
      }
    }
  }
}
