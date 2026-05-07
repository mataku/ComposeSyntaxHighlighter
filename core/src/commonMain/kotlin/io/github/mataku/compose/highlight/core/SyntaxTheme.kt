package io.github.mataku.compose.highlight.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Maps tree-sitter highlight captures to [SpanStyle] values.
 *
 * Set the named field for each capture you want styled. Unset fields fall back to a parent
 * prefix (e.g. `string.escape` falls back to [string]), and finally to [baseStyle]. For
 * grammar-specific captures not covered by a field (e.g. `keyword.return`, `variable.member`),
 * use [extras]; entries there win over typed fields for the same name.
 */
data class SyntaxTheme(
  /** Applied to the entire string before any capture-specific style. Set the default text color here. */
  val baseStyle: SpanStyle = SpanStyle(),
  /** Optional background color hint for the surrounding container. Not applied by [SyntaxHighlightedText]. */
  val background: Color? = null,
  /** Style for `keyword` captures. */
  val keyword: SpanStyle? = null,
  /** Style for `function` captures. */
  val function: SpanStyle? = null,
  /** Style for `type` captures. */
  val type: SpanStyle? = null,
  /** Style for `string` captures. */
  val string: SpanStyle? = null,
  /** Style for `string.escape` captures. Falls back to [string] when unset. */
  val stringEscape: SpanStyle? = null,
  /** Style for `number` captures. */
  val number: SpanStyle? = null,
  /** Style for `boolean` captures. */
  val boolean: SpanStyle? = null,
  /** Style for `comment` captures. */
  val comment: SpanStyle? = null,
  /** Style for `constant` captures. */
  val constant: SpanStyle? = null,
  /** Style for `property` captures. */
  val property: SpanStyle? = null,
  /** Style for `variable` captures. */
  val variable: SpanStyle? = null,
  /** Style for `namespace` captures. */
  val namespace: SpanStyle? = null,
  /** Style for `operator` captures. */
  val operator: SpanStyle? = null,
  /** Style for `punctuation` captures. */
  val punctuation: SpanStyle? = null,
  /** Overrides for capture names not represented as typed fields. Wins over typed fields for the same name. */
  val extras: Map<String, SpanStyle> = emptyMap(),
) {
  /**
   * Returns the [SpanStyle] for [captureName], falling back along dotted prefixes.
   *
   * For a capture like `string.escape`, the lookup tries `string.escape`, then `string`, and
   * finally returns `null` if no entry exists. [extras] wins over typed fields for the same name.
   */
  fun resolve(captureName: String): SpanStyle? {
    lookup(captureName)?.let { return it }
    var dot = captureName.lastIndexOf('.')
    while (dot > 0) {
      val prefix = captureName.substring(0, dot)
      lookup(prefix)?.let { return it }
      dot = prefix.lastIndexOf('.')
    }
    return null
  }

  private fun lookup(name: String): SpanStyle? = extras[name] ?: fieldFor(name)

  private fun fieldFor(name: String): SpanStyle? = when (name) {
    "keyword" -> keyword
    "function" -> function
    "type" -> type
    "string" -> string
    "string.escape" -> stringEscape
    "number" -> number
    "boolean" -> boolean
    "comment" -> comment
    "constant" -> constant
    "property" -> property
    "variable" -> variable
    "namespace" -> namespace
    "operator" -> operator
    "punctuation" -> punctuation
    else -> null
  }

  companion object {
    /** VSCode-inspired neutral dark theme. Used as the default for [LocalSyntaxTheme]. */
    val DarkDefault: SyntaxTheme by lazy {
      SyntaxTheme(
        baseStyle = SpanStyle(color = Color(0xFFE0E0E0)),
        background = Color(0xFF1E1E1E),
        keyword = SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold),
        function = SpanStyle(color = Color(0xFFDCDCAA)),
        type = SpanStyle(color = Color(0xFF4EC9B0)),
        string = SpanStyle(color = Color(0xFFCE9178)),
        stringEscape = SpanStyle(color = Color(0xFFD7BA7D)),
        number = SpanStyle(color = Color(0xFFB5CEA8)),
        boolean = SpanStyle(color = Color(0xFF569CD6)),
        comment = SpanStyle(color = Color(0xFF6A9955)),
        constant = SpanStyle(color = Color(0xFF4FC1FF)),
        property = SpanStyle(color = Color(0xFF9CDCFE)),
        variable = SpanStyle(color = Color(0xFF9CDCFE)),
        namespace = SpanStyle(color = Color(0xFF4EC9B0)),
        operator = SpanStyle(color = Color(0xFFD4D4D4)),
        punctuation = SpanStyle(color = Color(0xFFD4D4D4)),
      )
    }

    /** VSCode-inspired neutral light theme. */
    val LightDefault: SyntaxTheme by lazy {
      SyntaxTheme(
        baseStyle = SpanStyle(color = Color(0xFF1F1F1F)),
        background = Color(0xFFFFFFFF),
        keyword = SpanStyle(color = Color(0xFFAF00DB), fontWeight = FontWeight.Bold),
        function = SpanStyle(color = Color(0xFF795E26)),
        type = SpanStyle(color = Color(0xFF267F99)),
        string = SpanStyle(color = Color(0xFFA31515)),
        stringEscape = SpanStyle(color = Color(0xFFEE0000)),
        number = SpanStyle(color = Color(0xFF098658)),
        boolean = SpanStyle(color = Color(0xFF0000FF)),
        comment = SpanStyle(color = Color(0xFF008000)),
        constant = SpanStyle(color = Color(0xFF0070C1)),
        property = SpanStyle(color = Color(0xFF001080)),
        variable = SpanStyle(color = Color(0xFF001080)),
        namespace = SpanStyle(color = Color(0xFF267F99)),
        operator = SpanStyle(color = Color(0xFF000000)),
        punctuation = SpanStyle(color = Color(0xFF000000)),
      )
    }

    /** Ethan Schoonover's Solarized Dark (base03 background). Attribution in META-INF/NOTICE. */
    val SolarizedDark: SyntaxTheme by lazy {
      SyntaxTheme(
        baseStyle = SpanStyle(color = Color(0xFF839496)),
        background = Color(0xFF002B36),
        keyword = SpanStyle(color = Color(0xFF859900), fontWeight = FontWeight.Bold),
        function = SpanStyle(color = Color(0xFF268BD2)),
        type = SpanStyle(color = Color(0xFFB58900)),
        string = SpanStyle(color = Color(0xFF2AA198)),
        stringEscape = SpanStyle(color = Color(0xFFCB4B16)),
        number = SpanStyle(color = Color(0xFFD33682)),
        boolean = SpanStyle(color = Color(0xFF6C71C4)),
        comment = SpanStyle(color = Color(0xFF586E75)),
        constant = SpanStyle(color = Color(0xFFCB4B16)),
        property = SpanStyle(color = Color(0xFF268BD2)),
        variable = SpanStyle(color = Color(0xFF839496)),
        namespace = SpanStyle(color = Color(0xFFB58900)),
        operator = SpanStyle(color = Color(0xFF93A1A1)),
        punctuation = SpanStyle(color = Color(0xFF586E75)),
      )
    }

    /** Ethan Schoonover's Solarized Light (base3 background). Attribution in META-INF/NOTICE. */
    val SolarizedLight: SyntaxTheme by lazy {
      SyntaxTheme(
        baseStyle = SpanStyle(color = Color(0xFF657B83)),
        background = Color(0xFFFDF6E3),
        keyword = SpanStyle(color = Color(0xFF859900), fontWeight = FontWeight.Bold),
        function = SpanStyle(color = Color(0xFF268BD2)),
        type = SpanStyle(color = Color(0xFFB58900)),
        string = SpanStyle(color = Color(0xFF2AA198)),
        stringEscape = SpanStyle(color = Color(0xFFCB4B16)),
        number = SpanStyle(color = Color(0xFFD33682)),
        boolean = SpanStyle(color = Color(0xFF6C71C4)),
        comment = SpanStyle(color = Color(0xFF93A1A1)),
        constant = SpanStyle(color = Color(0xFFCB4B16)),
        property = SpanStyle(color = Color(0xFF268BD2)),
        variable = SpanStyle(color = Color(0xFF657B83)),
        namespace = SpanStyle(color = Color(0xFFB58900)),
        operator = SpanStyle(color = Color(0xFF586E75)),
        punctuation = SpanStyle(color = Color(0xFF93A1A1)),
      )
    }

    /** GitHub Primer Dark syntax tokens. Attribution in META-INF/NOTICE. */
    val GitHubDark: SyntaxTheme by lazy {
      SyntaxTheme(
        baseStyle = SpanStyle(color = Color(0xFFC9D1D9)),
        background = Color(0xFF0D1117),
        keyword = SpanStyle(color = Color(0xFFFF7B72), fontWeight = FontWeight.Bold),
        function = SpanStyle(color = Color(0xFFD2A8FF)),
        type = SpanStyle(color = Color(0xFFFFA657)),
        string = SpanStyle(color = Color(0xFFA5D6FF)),
        stringEscape = SpanStyle(color = Color(0xFF79C0FF)),
        number = SpanStyle(color = Color(0xFF79C0FF)),
        boolean = SpanStyle(color = Color(0xFF79C0FF)),
        comment = SpanStyle(color = Color(0xFF8B949E)),
        constant = SpanStyle(color = Color(0xFF79C0FF)),
        property = SpanStyle(color = Color(0xFF79C0FF)),
        variable = SpanStyle(color = Color(0xFFFFA657)),
        namespace = SpanStyle(color = Color(0xFFFF7B72)),
        operator = SpanStyle(color = Color(0xFFFF7B72)),
        punctuation = SpanStyle(color = Color(0xFFC9D1D9)),
      )
    }

    /** GitHub Primer Light syntax tokens. Attribution in META-INF/NOTICE. */
    val GitHubLight: SyntaxTheme by lazy {
      SyntaxTheme(
        baseStyle = SpanStyle(color = Color(0xFF1F2328)),
        background = Color(0xFFFFFFFF),
        keyword = SpanStyle(color = Color(0xFFCF222E), fontWeight = FontWeight.Bold),
        function = SpanStyle(color = Color(0xFF8250DF)),
        type = SpanStyle(color = Color(0xFF953800)),
        string = SpanStyle(color = Color(0xFF0A3069)),
        stringEscape = SpanStyle(color = Color(0xFF0550AE)),
        number = SpanStyle(color = Color(0xFF0550AE)),
        boolean = SpanStyle(color = Color(0xFF0550AE)),
        comment = SpanStyle(color = Color(0xFF6E7781)),
        constant = SpanStyle(color = Color(0xFF0550AE)),
        property = SpanStyle(color = Color(0xFF0550AE)),
        variable = SpanStyle(color = Color(0xFF953800)),
        namespace = SpanStyle(color = Color(0xFFCF222E)),
        operator = SpanStyle(color = Color(0xFFCF222E)),
        punctuation = SpanStyle(color = Color(0xFF1F2328)),
      )
    }

    /** Atom One Dark (atom/atom one-dark-syntax). Attribution in META-INF/NOTICE. */
    val OneDark: SyntaxTheme by lazy {
      SyntaxTheme(
        baseStyle = SpanStyle(color = Color(0xFFABB2BF)),
        background = Color(0xFF282C34),
        keyword = SpanStyle(color = Color(0xFFC678DD), fontWeight = FontWeight.Bold),
        function = SpanStyle(color = Color(0xFF61AFEF)),
        type = SpanStyle(color = Color(0xFFE5C07B)),
        string = SpanStyle(color = Color(0xFF98C379)),
        stringEscape = SpanStyle(color = Color(0xFF56B6C2)),
        number = SpanStyle(color = Color(0xFFD19A66)),
        boolean = SpanStyle(color = Color(0xFFD19A66)),
        comment = SpanStyle(color = Color(0xFF5C6370)),
        constant = SpanStyle(color = Color(0xFFD19A66)),
        property = SpanStyle(color = Color(0xFFE06C75)),
        variable = SpanStyle(color = Color(0xFFE06C75)),
        namespace = SpanStyle(color = Color(0xFFE5C07B)),
        operator = SpanStyle(color = Color(0xFFC678DD)),
        punctuation = SpanStyle(color = Color(0xFFABB2BF)),
      )
    }

    /** Atom One Light (atom/atom one-light-syntax). Attribution in META-INF/NOTICE. */
    val OneLight: SyntaxTheme by lazy {
      SyntaxTheme(
        baseStyle = SpanStyle(color = Color(0xFF383A42)),
        background = Color(0xFFFAFAFA),
        keyword = SpanStyle(color = Color(0xFFA626A4), fontWeight = FontWeight.Bold),
        function = SpanStyle(color = Color(0xFF4078F2)),
        type = SpanStyle(color = Color(0xFFC18401)),
        string = SpanStyle(color = Color(0xFF50A14F)),
        stringEscape = SpanStyle(color = Color(0xFF0184BC)),
        number = SpanStyle(color = Color(0xFF986801)),
        boolean = SpanStyle(color = Color(0xFF986801)),
        comment = SpanStyle(color = Color(0xFFA0A1A7)),
        constant = SpanStyle(color = Color(0xFF986801)),
        property = SpanStyle(color = Color(0xFFE45649)),
        variable = SpanStyle(color = Color(0xFFE45649)),
        namespace = SpanStyle(color = Color(0xFFC18401)),
        operator = SpanStyle(color = Color(0xFFA626A4)),
        punctuation = SpanStyle(color = Color(0xFF383A42)),
      )
    }

    /** Dracula (dark only — there is no canonical light variant). Attribution in META-INF/NOTICE. */
    val Dracula: SyntaxTheme by lazy {
      SyntaxTheme(
        baseStyle = SpanStyle(color = Color(0xFFF8F8F2)),
        background = Color(0xFF282A36),
        keyword = SpanStyle(color = Color(0xFFFF79C6), fontWeight = FontWeight.Bold),
        function = SpanStyle(color = Color(0xFF50FA7B)),
        type = SpanStyle(color = Color(0xFF8BE9FD)),
        string = SpanStyle(color = Color(0xFFF1FA8C)),
        stringEscape = SpanStyle(color = Color(0xFFFFB86C)),
        number = SpanStyle(color = Color(0xFFBD93F9)),
        boolean = SpanStyle(color = Color(0xFFBD93F9)),
        comment = SpanStyle(color = Color(0xFF6272A4)),
        constant = SpanStyle(color = Color(0xFFBD93F9)),
        property = SpanStyle(color = Color(0xFF50FA7B)),
        variable = SpanStyle(color = Color(0xFFF8F8F2)),
        namespace = SpanStyle(color = Color(0xFF8BE9FD)),
        operator = SpanStyle(color = Color(0xFFFF79C6)),
        punctuation = SpanStyle(color = Color(0xFFF8F8F2)),
      )
    }
  }
}
