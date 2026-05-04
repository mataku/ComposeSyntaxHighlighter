package com.mataku.composesyntaxhighlighter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            KotlinHighlightDemo()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
  MaterialTheme {
    Surface {
      KotlinHighlightDemo()
    }
  }
}
