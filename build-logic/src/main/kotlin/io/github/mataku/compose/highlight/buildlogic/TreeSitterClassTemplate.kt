package io.github.mataku.compose.highlight.buildlogic

/**
 * Renders the common-source-set `expect` declaration for a secondary grammar.
 */
internal fun renderCommonTreeSitterClass(packageName: String, className: String): String = """// Automatically generated file. DO NOT MODIFY

package $packageName

expect object $className {
    fun language(): Any
}
"""

/**
 * Renders the JVM-source-set `actual` implementation for a secondary grammar.
 * [libName] is the shared library this class loads — for a secondary grammar this is the
 * parent module's `languageName` (so all grammars in the module map to one .so).
 */
internal fun renderJvmTreeSitterClass(
  packageName: String,
  className: String,
  libName: String,
  cSymbol: String,
): String = """// Automatically generated file. DO NOT MODIFY

package $packageName

import java.io.File.createTempFile
import javax.annotation.processing.Generated

@Suppress("FunctionName")
@Generated("io.github.mataku.compose.highlight.buildlogic")
actual object $className {
    private const val LIB_NAME = "$libName"

    init {
        try {
            System.loadLibrary(LIB_NAME)
        } catch (ex: UnsatisfiedLinkError) {
            @Suppress("UnsafeDynamicallyLoadedCode")
            System.load(libPath() ?: throw ex)
        }
    }

    actual fun language(): Any = $cSymbol()

    @JvmStatic
    private external fun $cSymbol(): Long

    @JvmStatic
    @Suppress("ConvertToStringTemplate")
    @Throws(UnsupportedOperationException::class)
    internal fun libPath(): String? {
        val osName = System.getProperty("os.name")!!.lowercase()
        val archName = System.getProperty("os.arch")!!.lowercase()
        val ext: String
        val os: String
        val prefix: String
        when {
            "windows" in osName -> {
                ext = "dll"
                os = "windows"
                prefix = ""
            }
            "linux" in osName -> {
                ext = "so"
                os = "linux"
                prefix = "lib"
            }
            "mac" in osName -> {
                ext = "dylib"
                os = "macos"
                prefix = "lib"
            }
            else -> {
                throw UnsupportedOperationException("Unsupported operating system: " + osName)
            }
        }
        val arch = when {
            "amd64" in archName || "x86_64" in archName -> "x64"
            "aarch64" in archName || "arm64" in archName -> "aarch64"
            else -> throw UnsupportedOperationException("Unsupported architecture: " + archName)
        }
        val libPath = "/lib/" + os + "/" + arch + "/" + prefix + LIB_NAME + "." + ext
        val libUrl = javaClass.getResource(libPath) ?: return null
        return createTempFile(prefix + LIB_NAME, "." + ext).apply {
            writeBytes(libUrl.openStream().use { it.readAllBytes() })
            deleteOnExit()
        }.path
    }
}
"""

/**
 * Renders the Android-source-set `actual` implementation for a secondary grammar.
 * Android does not need the jar-resource fallback path; `System.loadLibrary` resolves
 * from the APK's `lib/<abi>/` directly.
 */
internal fun renderAndroidTreeSitterClass(
  packageName: String,
  className: String,
  libName: String,
  cSymbol: String,
): String = """// Automatically generated file. DO NOT MODIFY

package $packageName

import dalvik.annotation.optimization.CriticalNative
import javax.annotation.processing.Generated

@Suppress("FunctionName")
@Generated("io.github.mataku.compose.highlight.buildlogic")
actual object $className {
    init {
        System.loadLibrary("$libName")
    }

    actual fun language(): Any = $cSymbol()

    @JvmStatic
    @CriticalNative
    private external fun $cSymbol(): Long
}
"""

/**
 * Renders the Native (iOS) `actual` implementation for a secondary grammar.
 * The C symbol is reached through cinterop bindings that ktreesitter-plugin emits into
 * the primary grammar's package. The unified header at
 * `build/generated/iosHeaders/tree-sitter-<langName>.h` declares every grammar's
 * `tree_sitter_*` symbol, so cinterop emits Kotlin bindings for all of them in the
 * same package.
 *
 * - [cinteropPackage] e.g. `io.github.mataku.compose.highlight.markdown.internal` —
 *   the package where cinterop emits bindings (= primary grammar's packageName).
 */
internal fun renderNativeTreeSitterClass(
  packageName: String,
  className: String,
  cinteropPackage: String,
  cSymbol: String,
): String = """// Automatically generated file. DO NOT MODIFY

package $packageName

import $cinteropPackage.$cSymbol
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual object $className {
    actual fun language(): Any = $cSymbol()!!
}
"""
