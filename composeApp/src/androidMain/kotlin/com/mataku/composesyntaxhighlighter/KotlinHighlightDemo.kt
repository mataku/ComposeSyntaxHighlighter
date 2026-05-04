package com.mataku.composesyntaxhighlighter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mataku.compose.syntax.core.SyntaxHighlightedText
import io.github.mataku.compose.syntax.core.SyntaxTheme
import io.github.mataku.compose.syntax.language.kotlin.KotlinLanguage

private val sampleCode = """
    package com.example

    import kotlinx.coroutines.flow.Flow

    data class User(val id: Long, val name: String)

    class UserRepository(private val api: Api) {
        fun observe(id: Long): Flow<User> = api.streamUser(id).map { dto ->
            User(id = dto.id, name = dto.name ?: "anonymous")
        }
    }

    fun main() {
        val n = 42
        println("hello, world: ${'$'}n")
    }
""".trimIndent()

private val darkTheme = SyntaxTheme(
    baseStyle = SpanStyle(color = Color(0xFFE0E0E0)),
    styles = mapOf(
        "keyword" to SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold),
        "keyword.function" to SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold),
        "keyword.return" to SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold),
        "function" to SpanStyle(color = Color(0xFFDCDCAA)),
        "function.builtin" to SpanStyle(color = Color(0xFFDCDCAA)),
        "type" to SpanStyle(color = Color(0xFF4EC9B0)),
        "type.builtin" to SpanStyle(color = Color(0xFF4EC9B0)),
        "string" to SpanStyle(color = Color(0xFFCE9178)),
        "string.escape" to SpanStyle(color = Color(0xFFD7BA7D)),
        "number" to SpanStyle(color = Color(0xFFB5CEA8)),
        "boolean" to SpanStyle(color = Color(0xFF569CD6)),
        "comment" to SpanStyle(color = Color(0xFF6A9955)),
        "constant" to SpanStyle(color = Color(0xFF4FC1FF)),
        "property" to SpanStyle(color = Color(0xFF9CDCFE)),
        "variable" to SpanStyle(color = Color(0xFF9CDCFE)),
        "namespace" to SpanStyle(color = Color(0xFF4EC9B0)),
        "operator" to SpanStyle(color = Color(0xFFD4D4D4)),
        "punctuation.bracket" to SpanStyle(color = Color(0xFFD4D4D4)),
        "punctuation.delimiter" to SpanStyle(color = Color(0xFFD4D4D4)),
    ),
)

@Composable
fun KotlinHighlightDemo() {
    MaterialTheme {
        Surface(
          color = Color(0xFF1E1E1E),
          modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                  .safeContentPadding(),
            ) {
                SyntaxHighlightedText(
                    code = sampleCode,
                    language = KotlinLanguage,
                    theme = darkTheme,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                    ),
                )
            }
        }
    }
}
