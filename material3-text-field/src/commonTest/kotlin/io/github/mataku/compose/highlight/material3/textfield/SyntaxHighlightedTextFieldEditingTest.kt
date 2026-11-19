package io.github.mataku.compose.highlight.material3.textfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.kotlin.Kotlin
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SyntaxHighlightedTextFieldEditingTest {

  private val theme = SyntaxTheme(
    keyword = SpanStyle(color = Color.Red),
    string = SpanStyle(color = Color.Green),
    number = SpanStyle(color = Color.Blue),
  )
  private val textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 16.sp)

  @Test
  fun typingAppendsAtCaretAndAdvancesSelection() = runComposeUiTest {
    val field = setHighlightedField(TextFieldValue("val a = 1", selection = TextRange(9)))

    onNodeWithTag(FIELD).performTextInput("日本語")

    assertEquals("val a = 1日本語", field.value.text)
    assertEquals(TextRange(12), field.value.selection)
  }

  @Test
  fun typingInsideMultibyteTextInsertsAtTheCaret() = runComposeUiTest {
    val code = "val s = \"日本語\""
    val field = setHighlightedField(TextFieldValue(code))
    val caret = code.indexOf("本")

    onNodeWithTag(FIELD).performTextInputSelection(TextRange(caret))
    onNodeWithTag(FIELD).performTextInput("😀")

    assertEquals("val s = \"日😀本語\"", field.value.text)
    assertEquals(TextRange(caret + "😀".length), field.value.selection)
  }

  @Test
  fun typingReplacesTheSelectedMultibyteRange() = runComposeUiTest {
    val code = "val s = \"日本語\" // 末尾"
    val field = setHighlightedField(TextFieldValue(code))
    val start = code.indexOf("日")

    onNodeWithTag(FIELD).performTextInputSelection(TextRange(start, start + 2))
    assertEquals(TextRange(start, start + 2), field.value.selection)
    onNodeWithTag(FIELD).performTextInput("x")

    assertEquals("val s = \"x語\" // 末尾", field.value.text)
    assertEquals(TextRange(start + 1), field.value.selection)
  }

  @Test
  fun typingWhileTheHighlightIsStaleKeepsTextAndSelectionExact() = runComposeUiTest {
    val field = setHighlightedField(TextFieldValue("val a = \"日本語\"", selection = TextRange(0)))

    // Each input lands before the asynchronous highlight of the previous text can be awaited.
    onNodeWithTag(FIELD).performTextInput("// 😀\n")
    onNodeWithTag(FIELD).performTextInputSelection(TextRange(0, field.value.text.length))
    onNodeWithTag(FIELD).performTextInput("x")

    assertEquals("x", field.value.text)
    assertEquals(TextRange(1), field.value.selection)
    waitUntil(timeoutMillis = 2_000) { layoutOf(FIELD).layoutInput.text.text == "x" }
  }

  @Test
  fun highlightedLayoutReportsTheValueTextAndSelection() = runComposeUiTest {
    val code = "val s = \"日本語😀\"\nval n = 1"
    val selection = TextRange(code.indexOf("本"), code.indexOf("😀"))
    setHighlightedField(TextFieldValue(code, selection = selection))

    waitUntil(timeoutMillis = 2_000) { layoutOf(FIELD).layoutInput.text.spanStyles.isNotEmpty() }

    assertEquals(code, layoutOf(FIELD).layoutInput.text.text)
    val reported = onNodeWithTag(FIELD).fetchSemanticsNode().config.getOrNull(SemanticsProperties.TextSelectionRange)
    assertEquals(selection, reported)
  }

  @Test
  fun highlightDoesNotMoveTheCaretRelativeToAPlainField() = runComposeUiTest {
    val code = "val s = \"日本語😀\"\nval n = 1 // 末尾"
    setContent {
      MaterialTheme {
        Column {
          SyntaxHighlightedTextField(
            value = TextFieldValue(code),
            onValueChange = {},
            language = Languages.Kotlin,
            theme = theme,
            textStyle = textStyle,
            modifier = Modifier.testTag(FIELD),
          )
          BasicTextField(
            value = TextFieldValue(code),
            onValueChange = {},
            textStyle = textStyle,
            modifier = Modifier.testTag(PLAIN),
          )
        }
      }
    }

    waitUntil(timeoutMillis = 2_000) { layoutOf(FIELD).layoutInput.text.spanStyles.isNotEmpty() }

    val highlighted = layoutOf(FIELD)
    val plain = layoutOf(PLAIN)
    for (offset in 0..code.length) {
      assertEquals(plain.getCursorRect(offset), highlighted.getCursorRect(offset), "caret rect at offset $offset")
    }
  }

  private class FieldState(initial: TextFieldValue) {
    var value by mutableStateOf(initial)
  }

  private fun ComposeUiTest.setHighlightedField(initial: TextFieldValue): FieldState {
    val state = FieldState(initial)
    setContent {
      MaterialTheme {
        SyntaxHighlightedTextField(
          value = state.value,
          onValueChange = { state.value = it },
          language = Languages.Kotlin,
          theme = theme,
          textStyle = textStyle,
          modifier = Modifier.testTag(FIELD),
        )
      }
    }
    return state
  }

  private fun ComposeUiTest.layoutOf(tag: String): TextLayoutResult {
    val results = mutableListOf<TextLayoutResult>()
    onNodeWithTag(tag).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
    return results.single()
  }

  private companion object {
    const val FIELD = "field"
    const val PLAIN = "plain"
  }
}
