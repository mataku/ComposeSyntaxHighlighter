import io.github.mataku.compose.highlight.buildlogic.ComposeSyntaxHighlightLanguageExtension
import io.github.mataku.compose.highlight.buildlogic.GrammarSpec
import io.github.mataku.compose.highlight.buildlogic.extractParserAbi
import io.github.mataku.compose.highlight.buildlogic.writeAndroidCMakeLists
import io.github.mataku.compose.highlight.buildlogic.writeHostCMakeLists
import io.github.mataku.compose.highlight.buildlogic.writeIosHeader
import io.github.treesitter.ktreesitter.plugin.GrammarExtension
import io.github.treesitter.ktreesitter.plugin.GrammarFilesTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.android.kotlin.multiplatform.library")
  id("io.github.tree-sitter.ktreesitter-plugin")
  id("com.vanniktech.maven.publish")
  id("compose-syntax-highlight-kdoc")
  id("compose-syntax-highlight-spotless")
}

val composeSyntaxHighlightLanguage = extensions.create<ComposeSyntaxHighlightLanguageExtension>("composeSyntaxHighlightLanguage")

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun catalogVersionInt(alias: String): Int = versionCatalog.findVersion(alias).get().requiredVersion.toInt()

val highlightsQueryDir = layout.buildDirectory.dir("generated/highlights")
val hostCMakeWorkDir = layout.buildDirectory.dir("host-cmake")

// Used by the Android NDK CMake task chain. Mirrors the value from the
// pre-AGP-9 `android { ndkVersion = ... }` block. When the project bumps NDK,
// update both this value and the version pinned in CI's setup-android-deps action.
private val androidNdkVersion = "26.3.11579264"
private val androidAbis = listOf("arm64-v8a", "armeabi-v7a", "x86_64")
private val androidPlatform = "android-${catalogVersionInt("android-minSdk")}"
private val androidCMakeRootDir = layout.buildDirectory.dir("android-cmake")
private val androidJniLibsDir = layout.buildDirectory.dir("generated/jniLibs")

val noticeOutDir = layout.buildDirectory.dir("notice")
val iosHeaderDirProvider = layout.buildDirectory.dir("generated/iosHeaders")
val iosStaticLibsDirProvider = layout.buildDirectory.dir("libs")

val primaryGrammarProvider: Provider<GrammarSpec> = providers.provider {
  val grammars = composeSyntaxHighlightLanguage.grammars.toList()
  require(grammars.isNotEmpty()) { "composeSyntaxHighlightLanguage.grammars must contain at least one entry" }
  grammars.first()
}

val grammarAbisProvider: Provider<List<Pair<String, Int>>> = providers.provider {
  composeSyntaxHighlightLanguage.grammars.map { spec ->
    val grammarDir = projectDir.resolve(spec.submodulePath.get())
    val parserC = grammarDir.resolve("src/parser.c")
    require(parserC.exists()) {
      "parser.c not found for grammar '${spec.name}' at ${parserC.absolutePath}. " +
        "Ensure the submodule is checked out, or run :generateParserSource first."
    }
    spec.name to extractParserAbi(parserC)
  }
}

val primaryGrammarAbiProvider: Provider<Int> =
  grammarAbisProvider.map { it.first().second }

val grammarDirProvider: Provider<File> = primaryGrammarProvider.flatMap { spec ->
  spec.submodulePath.map { projectDir.resolve(it) }
}
val packageNameProvider: Provider<String> = primaryGrammarProvider.map {
  "io.github.mataku.compose.highlight.${it.name}.internal"
}
val highlightsPackageDirProvider: Provider<String> = primaryGrammarProvider.map {
  "io/github/mataku/compose/highlight/${it.name}"
}

extensions.configure<GrammarExtension>("grammar") {
  baseDir.set(grammarDirProvider)
  grammarName.set(primaryGrammarProvider.map { it.name })
  className.set(primaryGrammarProvider.flatMap { it.parserClassName })
  packageName.set(packageNameProvider)
  // Override ktreesitter's default `ktreesitter-<grammarName>`: we ship one shared
  // library per module, named after languageName (matches the CMakeLists target).
  // For single-grammar modules where grammarName == languageName this is a no-op.
  libraryName.set(composeSyntaxHighlightLanguage.languageName.map { "ktreesitter-$it" })
  files.set(
    primaryGrammarProvider.flatMap { it.sources }.zip(grammarDirProvider) { sources, grammarDir ->
      sources.map { grammarDir.resolve(it) }.toTypedArray()
    },
  )
}

val grammarCSymbolsProvider: Provider<List<String>> = providers.provider {
  composeSyntaxHighlightLanguage.grammars.map { spec ->
    spec.cSymbol.orNull ?: "tree_sitter_${spec.name}"
  }
}

val writeIosHeaderTask = tasks.register("writeIosHeader") {
  // The cinterop grammar.def emitted by ktreesitter-plugin references
  // tree-sitter-<primaryGrammarName>.h, so the iOS alias header must match.
  // For single-grammar modules this equals languageName.
  val nameProvider = primaryGrammarProvider.map { it.name }
  val symbolsProvider = grammarCSymbolsProvider
  val dirProvider = iosHeaderDirProvider.map { it.asFile }
  inputs.property("primaryGrammarName", nameProvider)
  inputs.property("grammarCSymbols", symbolsProvider)
  outputs.dir(dirProvider)
  doLast {
    writeIosHeader(dirProvider.get(), nameProvider.get(), symbolsProvider.get())
  }
}

val generateNotice = tasks.register("generateNotice") {
  val tplFile = rootProject.file("NOTICE.tpl")
  val outDirProvider = noticeOutDir.map { it.asFile }
  val grammarsProvider = providers.provider { composeSyntaxHighlightLanguage.grammars.map { it.name } }
  val licenseSpdxProvider = composeSyntaxHighlightLanguage.licenseSpdx
  val licenseSourceProvider = composeSyntaxHighlightLanguage.licenseSource
  inputs.file(tplFile)
  inputs.property("grammars", grammarsProvider)
  inputs.property("licenseSpdx", licenseSpdxProvider)
  inputs.property("licenseSource", licenseSourceProvider)
  outputs.dir(outDirProvider)
  doLast {
    val baseTpl = tplFile.readText()
    val entry = buildString {
      for (grammarName in grammarsProvider.get()) {
        appendLine()
        appendLine("Bundled grammar:")
        appendLine("  Component: tree-sitter-$grammarName")
        appendLine("  License: ${licenseSpdxProvider.get()}")
        appendLine("  Source: ${licenseSourceProvider.get()}")
      }
    }
    val outFile = File(outDirProvider.get(), "META-INF/NOTICE")
    outFile.parentFile.mkdirs()
    outFile.writeText(baseTpl + entry)
  }
}

val generateHighlightsQuery = tasks.register("generateHighlightsQuery") {
  val srcsProvider = primaryGrammarProvider.flatMap { it.queries }.zip(grammarDirProvider) { queries, grammarDir ->
    require(queries.isNotEmpty()) { "primary grammar.queries must contain at least one entry" }
    queries.map { grammarDir.resolve(it) }
  }
  val packageProvider = primaryGrammarProvider.map { it.name }
  val pkgDirProvider = highlightsPackageDirProvider
  val outDirProvider = highlightsQueryDir.map { it.asFile }
  inputs.files(srcsProvider)
  outputs.dir(outDirProvider)
  doLast {
    val srcs = srcsProvider.get()
    val outDir = outDirProvider.get()
    val text = srcs.joinToString(separator = "\n") { it.readText() }
      .replace("$", "\${'$'}")
    val out = File(outDir, "${pkgDirProvider.get()}/HighlightsQuery.kt")
    out.parentFile.mkdirs()
    out.writeText(
      """
            |package io.github.mataku.compose.highlight.${packageProvider.get()}
            |
            |internal const val HIGHLIGHTS_QUERY: String = ${'"'}${'"'}${'"'}
            |$text${'"'}${'"'}${'"'}
            |
      """.trimMargin(),
    )
  }
}

val generateParserSource = tasks.register<Exec>("generateParserSource") {
  val grammarDirFileProvider = grammarDirProvider
  val grammarJsProvider = grammarDirFileProvider.map { it.resolve("grammar.js") }
  val parserCProvider = grammarDirFileProvider.map { it.resolve("src/parser.c") }
  val localTreeSitterBin = rootProject.file("node_modules/.bin/tree-sitter")
  inputs.file(grammarJsProvider)
  outputs.file(parserCProvider)
  workingDir(grammarDirFileProvider)
  val abi = versionCatalog.findVersion("treesitterAbi").get().requiredVersion
  val resolvedCommand = if (localTreeSitterBin.exists()) localTreeSitterBin.absolutePath else "tree-sitter"
  commandLine(resolvedCommand, "generate", "--abi=$abi")
  doFirst {
    val available = if (localTreeSitterBin.exists()) {
      true
    } else {
      val check = ProcessBuilder("which", "tree-sitter")
        .redirectErrorStream(true)
        .start()
      check.waitFor()
      check.exitValue() == 0
    }
    if (!available) {
      error(
        "tree-sitter CLI was not found. The convention plugin prefers the project-local " +
          "install at <repo>/node_modules/.bin/tree-sitter and falls back to any " +
          "tree-sitter on PATH. To populate the project-local install, run your " +
          "package manager from the repository root (e.g. " +
          "`pnpm install --frozen-lockfile`; see package.json for the pinned " +
          "version). If lifecycle scripts are disabled in your environment " +
          "(e.g. ~/.npmrc sets ignore-scripts=true), trigger the CLI binary " +
          "download explicitly afterward (e.g. `pnpm rebuild tree-sitter-cli`).",
      )
    }
  }
  onlyIf {
    // Skip when parser.c is already present. Most grammar submodules commit
    // parser.c and the build trusts the checked-in artifact; tree-sitter-swift
    // is a notable exception (its upstream gitignores src/parser.c, so this
    // task fires on every fresh checkout). To force regeneration after editing
    // grammar.js, run `./gradlew :languages:<name>:generateParserSource --rerun`
    // — mtime-based comparison was unreliable in CI fresh checkouts and has
    // been removed.
    !parserCProvider.get().exists()
  }
}

val configureHostCMake = tasks.register<Exec>("configureHostCMake") {
  val workDirProvider = hostCMakeWorkDir.map { it.asFile }
  val srcDir = projectDir.resolve("host-cmake")
  inputs.file(srcDir.resolve("CMakeLists.txt"))
  outputs.file(workDirProvider.map { File(it, "CMakeCache.txt") })
  doFirst { workDirProvider.get().mkdirs() }
  workingDir(workDirProvider)
  commandLine("cmake", srcDir.absolutePath)
  dependsOn("generateGrammarFiles")
  dependsOn(generateParserSource)
}

val buildHostCMake = tasks.register<Exec>("buildHostCMake") {
  val workDirProvider = hostCMakeWorkDir.map { it.asFile }
  workingDir(workDirProvider)
  commandLine("cmake", "--build", ".")
  inputs.dir(projectDir.resolve("host-cmake"))
  inputs.dir(grammarDirProvider.map { it.resolve("src") })
  outputs.dir(workDirProvider)
  dependsOn(configureHostCMake)
}

// Per-ABI Android NDK CMake task chain. Mirrors host CMake but invokes cmake via
// the NDK toolchain file. Outputs land in build/generated/jniLibs/<abi>/, which
// is wired into androidMain's jniLibs source set so AGP packages the .so files
// into jni/<abi>/lib*.so inside the published AAR.

val androidCopyToJniLibsTasks: Map<String, TaskProvider<Copy>> = androidAbis.associateWith { abi ->
  val abiCapitalized = abi.replace("-", "").replaceFirstChar(Char::uppercaseChar)
  val workDirProvider = androidCMakeRootDir.map { it.dir(abi).asFile }
  val cmakeListsSrcDirProvider = providers.provider {
    projectDir.resolve("android-cmake/$abi")
  }
  val cmakeListsFileProvider = cmakeListsSrcDirProvider.map { File(it, "CMakeLists.txt") }
  val soFileProvider: Provider<File> = workDirProvider.map { workDir ->
    File(workDir, "libtree-sitter-${composeSyntaxHighlightLanguage.languageName.get()}.so")
  }
  val jniLibsDestDirProvider: Provider<File> = androidJniLibsDir.map { it.dir(abi).asFile }

  val writeCMakeListsTask = tasks.register("writeAndroidCMakeLists$abiCapitalized") {
    val languageNameProvider = composeSyntaxHighlightLanguage.languageName
    inputs.property("languageName", languageNameProvider)
    inputs.property("abi", abi)
    outputs.file(cmakeListsFileProvider)
    val grammarSpecsProvider: Provider<List<io.github.mataku.compose.highlight.buildlogic.GrammarBuildSpec>> =
      providers.provider {
        val grammars = composeSyntaxHighlightLanguage.grammars.toList()
        val primaryName = grammars.first().name
        grammars.map { spec ->
          io.github.mataku.compose.highlight.buildlogic.GrammarBuildSpec(
            name = spec.name,
            submodulePath = spec.submodulePath.get(),
            sources = spec.sources.get(),
            cSymbol = spec.cSymbol.orNull ?: "tree_sitter_${spec.name}",
            bindingCPath = if (spec.name == primaryName) {
              "build/generated/src/jni/binding.c"
            } else {
              "build/generated/src/jni/binding-${spec.name}.c"
            },
          )
        }
      }
    doLast {
      io.github.mataku.compose.highlight.buildlogic.writeAndroidNdkCmakeLists(
        cmakeListsFileProvider.get(),
        languageNameProvider.get(),
        grammarSpecsProvider.get(),
      )
    }
  }

  val configureTask = tasks.register<Exec>("configureAndroidCMake$abiCapitalized") {
    dependsOn(writeCMakeListsTask)
    dependsOn("generateGrammarFiles")
    dependsOn(generateParserSource)
    workingDir(workDirProvider)
    inputs.file(cmakeListsFileProvider)
    outputs.file(workDirProvider.map { File(it, "CMakeCache.txt") })
    val ndkRoot = io.github.mataku.compose.highlight.buildlogic.resolveAndroidNdkRoot(androidNdkVersion)
    commandLine(
      "cmake",
      "-DCMAKE_TOOLCHAIN_FILE=${ndkRoot.absolutePath}/build/cmake/android.toolchain.cmake",
      "-DANDROID_ABI=$abi",
      "-DANDROID_PLATFORM=$androidPlatform",
      "-DCMAKE_BUILD_TYPE=Release",
      cmakeListsSrcDirProvider.get().absolutePath,
    )
    val capturedWorkDir = workDirProvider
    doFirst { capturedWorkDir.get().mkdirs() }
  }

  val buildTask = tasks.register<Exec>("buildAndroidCMake$abiCapitalized") {
    dependsOn(configureTask)
    workingDir(workDirProvider)
    inputs.dir(workDirProvider)
    outputs.file(soFileProvider)
    commandLine("cmake", "--build", ".")
  }

  tasks.register<Copy>("copyAndroidSoTo${abiCapitalized}JniLibs") {
    dependsOn(buildTask)
    from(soFileProvider)
    into(jniLibsDestDirProvider)
  }
}

// Wire the per-ABI jniLibs output directory into the Android variant's source
// list. With com.android.kotlin.multiplatform.library there is no Kotlin source
// set or Android DSL surface that exposes jniLibs.srcDir(...); the
// KotlinMultiplatformAndroidComponentsExtension is the AGP-blessed path.
// addStaticSourceDirectory requires the directory to exist at configuration
// time, so the copy tasks create the parent ABI subdirs before the variant API
// inspects them.
extensions.configure<com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension>("androidComponents") {
  // namespace is read by AGP at variant creation time, which is after the
  // plugin script body but before afterEvaluate. finalizeDsl runs at the right
  // moment to pull from the consumer-populated extension.
  finalizeDsl { dsl ->
    dsl.namespace = "io.github.mataku.compose.highlight.${composeSyntaxHighlightLanguage.languageName.get()}"
  }
  onVariants { variant ->
    val jniLibsDirAbs = androidJniLibsDir.map { it.asFile.absolutePath }.get()
    File(jniLibsDirAbs).mkdirs()
    variant.sources.jniLibs?.addStaticSourceDirectory(jniLibsDirAbs)
  }
}

extensions.configure<KotlinMultiplatformExtension>("kotlin") {
  android {
    // namespace is set via androidComponents.finalizeDsl above; the consumer DSL block
    // (which populates languageName) has not yet run when this block executes.
    compileSdk = catalogVersionInt("android-compileSdk")
    minSdk = catalogVersionInt("android-minSdk")
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
    androidResources {
      enable = true
    }
    packaging {
      resources {
        excludes -= setOf("/META-INF/NOTICE", "/META-INF/NOTICE.txt", "/META-INF/NOTICE.md")
        pickFirsts += "/META-INF/NOTICE"
      }
    }
  }

  jvm()

  iosArm64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain {
      resources.srcDir(noticeOutDir)
      kotlin.srcDir(highlightsQueryDir)
      dependencies {
        api("${versionCatalog.findLibrary("coreApi").get().get().module}") {
          version {
            strictly(versionCatalog.findVersion("coreApiCompatibleRange").get().requiredVersion)
          }
        }
        api(versionCatalog.findLibrary("ktreesitter").get())
      }
    }
    getByName("jvmTest") {
      dependencies {
        implementation(versionCatalog.findLibrary("kotlin-test").get())
        implementation(versionCatalog.findLibrary("compose-ui").get())
        implementation(project(":core"))
      }
    }
  }

  sourceSets.named("androidMain") {
    resources.srcDir(noticeOutDir)
  }

  sourceSets.all {
    languageSettings.optIn("io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi")
  }
}

afterEvaluate {
  val languageName = composeSyntaxHighlightLanguage.languageName.orNull
    ?: error("composeSyntaxHighlightLanguage.languageName must be set in the consumer build script")

  val grammars = composeSyntaxHighlightLanguage.grammars.toList()
  require(grammars.isNotEmpty()) {
    "composeSyntaxHighlightLanguage.grammars { ... } must contain at least one create(...) entry"
  }
  val primary = grammars.first()
  val primarySubmodulePath = primary.submodulePath.orNull
    ?: error("grammars.${primary.name}.submodulePath must be set")
  primary.parserClassName.orNull
    ?: error("grammars.${primary.name}.parserClassName must be set")
  val primarySources = primary.sources.orNull?.takeIf { it.isNotEmpty() }
    ?: error("grammars.${primary.name}.sources must contain at least one C source path")
  primary.queries.orNull?.takeIf { it.isNotEmpty() }
    ?: error("grammars.${primary.name}.queries must contain at least the highlights.scm path")

  val primaryCSymbol = primary.cSymbol.orNull
  val primaryResolvedSymbol = primaryCSymbol ?: "tree_sitter_${primary.name}"

  val grammarBuildSpecs = grammars.map { spec ->
    val specName = spec.name
    val specSubmodulePath = spec.submodulePath.orNull
      ?: error("grammars.$specName.submodulePath must be set")
    val specSources = spec.sources.orNull?.takeIf { it.isNotEmpty() }
      ?: error("grammars.$specName.sources must contain at least one C source path")
    val specCSymbol = spec.cSymbol.orNull ?: "tree_sitter_$specName"
    val bindingPath = if (specName == primary.name) {
      "build/generated/src/jni/binding.c"
    } else {
      "build/generated/src/jni/binding-$specName.c"
    }
    io.github.mataku.compose.highlight.buildlogic.GrammarBuildSpec(
      name = specName,
      submodulePath = specSubmodulePath,
      sources = specSources,
      cSymbol = specCSymbol,
      bindingCPath = bindingPath,
    )
  }
  val secondaryGrammars = grammars.drop(1)
  writeAndroidCMakeLists(
    projectDir.resolve("CMakeLists.txt"),
    languageName,
    grammarBuildSpecs,
  )
  writeHostCMakeLists(
    projectDir.resolve("host-cmake/CMakeLists.txt"),
    languageName,
    grammarBuildSpecs,
  )

  primaryCSymbol?.let { symbol ->
    extensions.configure<GrammarExtension>("grammar") {
      languageMethods.set(mapOf("language" to symbol))
    }
  }

  val generateGrammarFilesTask = tasks.named<GrammarFilesTask>("generateGrammarFiles")
  generateGrammarFilesTask.configure {
    dependsOn(generateParserSource)
  }
  val generatedSrc = generateGrammarFilesTask.get().generatedSrc.get()
  extensions.configure<KotlinMultiplatformExtension>("kotlin") {
    sourceSets.configureEach {
      kotlin.srcDir(generatedSrc.dir(this.name).dir("kotlin"))
    }
    targets.withType<KotlinNativeTarget>().configureEach {
      val interopName = "treesitter${languageName.replaceFirstChar(Char::uppercaseChar)}"
      val headerDir = iosHeaderDirProvider.map { it.asFile }
      val libsDir = iosStaticLibsDirProvider
      val konanTargetName = konanTarget.name
      compilations.configureEach {
        cinterops.register(interopName) {
          definitionFile.set(generateGrammarFilesTask.flatMap { it.interopFile })
          includeDirs.allHeaders(headerDir)
          extraOpts(
            "-libraryPath",
            libsDir.get().dir(konanTargetName).asFile.absolutePath,
          )
          val taskName = interopProcessingTaskName
          tasks.matching { it.name == taskName }.configureEach {
            dependsOn(writeIosHeaderTask)
            dependsOn(generateGrammarFilesTask)
          }
        }
      }
    }
  }

  tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateGrammarFilesTask, generateHighlightsQuery)
  }

  tasks.matching {
    it.name.endsWith("SourcesJar", ignoreCase = true)
  }.configureEach {
    dependsOn(generateGrammarFilesTask, generateHighlightsQuery)
  }

  tasks.matching {
    it.name.startsWith("configureCMake") || it.name.startsWith("buildCMake")
  }.configureEach {
    dependsOn(generateGrammarFilesTask)
    dependsOn(generateParserSource)
  }

  // iOS static library build: compile every grammar's parser.c (+ scanner.c) under the
  // Konan-bundled clang and archive the per-grammar object files into one
  // libtree-sitter-<primaryGrammarName>.a that cinterop links against (the
  // generated grammar.def references this exact filename). For single-grammar
  // modules the primary name equals languageName. Mirrors the languages/java
  // pattern in upstream kotlin-tree-sitter, extended for multi-grammar modules
  // (separate clang invocation per grammar to keep parser.o / scanner.o file
  // names from colliding across submodules).
  val grammarCompileInputs = grammarBuildSpecs.map { spec ->
    val grammarDir = projectDir.resolve(spec.submodulePath)
    val sourceFiles = spec.sources.map { grammarDir.resolve(it) }
    Triple(grammarDir, sourceFiles, spec.name == primary.name)
  }
  val secondaryParserTaskNames = secondaryGrammars.map { secondary ->
    "generateParserSource${secondary.name.replaceFirstChar(Char::uppercaseChar)}"
  }
  @Suppress("DEPRECATION")
  tasks.withType<CInteropProcess>().configureEach {
    if (name.startsWith("cinteropTest")) return@configureEach

    val konanHomePath = konanHome.get()
    val target = konanTarget
    val libFile = iosStaticLibsDirProvider.get()
      .dir(target.name)
      .file("libtree-sitter-${primary.name}.a")
      .asFile
    val allObjectFiles = grammarCompileInputs.flatMap { (grammarDir, sourceFiles, _) ->
      sourceFiles.map { grammarDir.resolve("${it.nameWithoutExtension}.o") }
    }
    val compileInputs = grammarCompileInputs

    dependsOn(generateParserSource)
    dependsOn(generateGrammarFilesTask)
    for (taskName in secondaryParserTaskNames) {
      dependsOn(taskName)
    }

    compileInputs.forEach { (_, sourceFiles, _) ->
      inputs.files(*sourceFiles.toTypedArray())
    }
    outputs.file(libFile)

    doFirst {
      val runKonan = File(konanHomePath, "bin/run_konan").absolutePath
      libFile.parentFile.mkdirs()

      for ((grammarDir, sourceFiles, _) in compileInputs) {
        val argsFile = File.createTempFile("args", null)
        argsFile.deleteOnExit()
        argsFile.writer().use { w ->
          w.write("-I${grammarDir.resolve("src").absolutePath}\n")
          w.write("-DTREE_SITTER_HIDE_SYMBOLS\n")
          w.write("-fvisibility=hidden\n")
          w.write("-std=c11\n")
          w.write("-O2\n")
          w.write("-g\n")
          w.write("-c\n")
          sourceFiles.forEach { w.write("${it.absolutePath}\n") }
        }
        val clangProc = ProcessBuilder(runKonan, "clang", "clang", target.name, "@${argsFile.path}")
          .directory(grammarDir)
          .redirectErrorStream(true)
          .start()
        val clangOut = clangProc.inputStream.bufferedReader().readText()
        val clangExit = clangProc.waitFor()
        if (clangExit != 0) {
          error("run_konan clang failed for ${grammarDir.name} (target=${target.name}, exit=$clangExit):\n$clangOut")
        }
      }

      val arArgs = buildList {
        add(runKonan)
        add("llvm")
        add("llvm-ar")
        add("rcs")
        add(libFile.absolutePath)
        addAll(allObjectFiles.map { it.absolutePath })
      }
      val arProc = ProcessBuilder(arArgs)
        .redirectErrorStream(true)
        .start()
      val arOut = arProc.inputStream.bufferedReader().readText()
      val arExit = arProc.waitFor()
      if (arExit != 0) {
        error("run_konan llvm-ar failed (exit=$arExit):\n$arOut")
      }
    }
  }

  tasks.named<Test>("jvmTest") {
    dependsOn(buildHostCMake)
    val libDirProvider = hostCMakeWorkDir.map { it.asFile.absolutePath }
    doFirst {
      systemProperty("java.library.path", libDirProvider.get())
    }
  }

  tasks.matching {
    val n = it.name
    n.endsWith("ProcessResources") ||
      n.startsWith("copyNonXmlValueResources") ||
      n.startsWith("convertXmlValueResources") ||
      n == "mergeReleaseJavaResource" ||
      n == "mergeDebugJavaResource" ||
      n == "processReleaseJavaRes" ||
      n == "processDebugJavaRes" ||
      n == "processAndroidMainJavaRes"
  }.configureEach {
    dependsOn(generateNotice)
  }

  // Make Android packaging depend on the per-ABI .so being staged in jniLibs.
  // AGP merges jniLibs source dirs during merge*JniLibFolders / bundleAar tasks.
  tasks.matching { task ->
    val name = task.name
    name.startsWith("merge") && name.endsWith("JniLibFolders") ||
      name == "bundleAndroidMainAar" ||
      name == "syncAndroidMainLibJars"
  }.configureEach {
    androidCopyToJniLibsTasks.values.forEach { copy -> dependsOn(copy) }
  }

  // AGP's prepareAndroidMainArtProfile inspects build/generated/src/*/baselineProfiles
  // — a sibling of the kotlin/ output that ktreesitter's generateGrammarFiles emits.
  // Declare the ordering so Gradle doesn't flag an implicit dependency.
  tasks.matching { it.name == "prepareAndroidMainArtProfile" }.configureEach {
    mustRunAfter(generateGrammarFilesTask)
  }

  val mavenPublishing = extensions.getByType(com.vanniktech.maven.publish.MavenPublishBaseExtension::class.java)
  mavenPublishing.coordinates(artifactId = "compose-syntax-highlight-$languageName")
  mavenPublishing.pom {
    name.set("Compose Syntax Highlight $languageName")
    description.set("$languageName syntax highlighting for Compose Multiplatform powered by tree-sitter")
  }
  mavenPublishing.pom {
    properties.put("tree-sitter-abi", primaryGrammarAbiProvider.map { it.toString() })
    for (secondary in secondaryGrammars) {
      val name = secondary.name
      properties.put(
        "tree-sitter-abi-$name",
        grammarAbisProvider.map { abis -> abis.first { it.first == name }.second.toString() },
      )
    }
  }

  // Secondary grammars (entries 2..N in grammars container). The primary entry is wired
  // via ktreesitter-plugin's GrammarExtension above; secondaries get hand-rolled task
  // graph here that mirrors what ktreesitter generates for the primary.
  for (secondary in secondaryGrammars) {
    val name = secondary.name
    val submodulePath = secondary.submodulePath.orNull
      ?: error("grammars.$name.submodulePath must be set")
    val parserClassName = secondary.parserClassName.orNull
      ?: error("grammars.$name.parserClassName must be set")
    val srcs = secondary.sources.orNull?.takeIf { it.isNotEmpty() }
      ?: error("grammars.$name.sources must contain at least one C source path")
    val qrys = secondary.queries.orNull?.takeIf { it.isNotEmpty() }
      ?: error("grammars.$name.queries must contain at least one query path")
    val symbol = secondary.cSymbol.orNull ?: "tree_sitter_$name"
    val grammarDir = projectDir.resolve(submodulePath)
    val packageName = "io.github.mataku.compose.highlight.$name.internal"
    val packagePath = "io/github/mataku/compose/highlight/$name/internal"
    val highlightsPkgPath = "io/github/mataku/compose/highlight/$name"
    val bindingCRel = "build/generated/src/jni/binding-$name.c"
    val nameCapitalized = name.replaceFirstChar { it.uppercaseChar() }

    val genParserTask = tasks.register<Exec>("generateParserSource$nameCapitalized") {
      val grammarJs = grammarDir.resolve("grammar.js")
      val parserC = grammarDir.resolve("src/parser.c")
      val localTreeSitterBin = rootProject.file("node_modules/.bin/tree-sitter")
      inputs.file(grammarJs)
      outputs.file(parserC)
      workingDir(grammarDir)
      val abi = versionCatalog.findVersion("treesitterAbi").get().requiredVersion
      val resolvedCommand = if (localTreeSitterBin.exists()) localTreeSitterBin.absolutePath else "tree-sitter"
      commandLine(resolvedCommand, "generate", "--abi=$abi")
      onlyIf { !parserC.exists() }
    }

    val genBindingTask = tasks.register("generateSecondaryBinding$nameCapitalized") {
      val outFile = projectDir.resolve(bindingCRel)
      // Must match the alias header emitted by writeAndroidCMakeLists, which is
      // tree-sitter-<primaryGrammarName>.h.
      val headerName = "tree-sitter-${primary.name}.h"
      outputs.file(outFile)
      doLast {
        outFile.parentFile.mkdirs()
        outFile.writeText(
          io.github.mataku.compose.highlight.buildlogic.renderSecondaryBindingC(
            packageName = packageName,
            className = parserClassName,
            cSymbol = symbol,
            headerName = headerName,
          ),
        )
      }
    }

    // ktreesitter-plugin emits cinterop bindings into <grammar.packageName>.internal.
    // The convention plugin's grammar { packageName = ... } is already
    // "io.github.mataku.compose.highlight.<primary>.internal", so the cinterop's
    // emitted package is "io.github.mataku.compose.highlight.<primary>.internal.internal".
    val cinteropEmittedPackage = "io.github.mataku.compose.highlight.${primary.name}.internal.internal"
    val genKotlinTask = tasks.register("generateSecondaryKotlin$nameCapitalized") {
      val commonDir = layout.buildDirectory.dir("generated/secondary/$name/commonMain/kotlin/$packagePath").get().asFile
      val jvmDir = layout.buildDirectory.dir("generated/secondary/$name/jvmMain/kotlin/$packagePath").get().asFile
      val androidDir = layout.buildDirectory.dir("generated/secondary/$name/androidMain/kotlin/$packagePath").get().asFile
      val iosDir = layout.buildDirectory.dir("generated/secondary/$name/iosMain/kotlin/$packagePath").get().asFile
      outputs.dir(layout.buildDirectory.dir("generated/secondary/$name"))
      doLast {
        commonDir.mkdirs()
        jvmDir.mkdirs()
        androidDir.mkdirs()
        iosDir.mkdirs()
        commonDir.resolve("$parserClassName.kt").writeText(
          io.github.mataku.compose.highlight.buildlogic.renderCommonTreeSitterClass(packageName, parserClassName),
        )
        jvmDir.resolve("$parserClassName.kt").writeText(
          io.github.mataku.compose.highlight.buildlogic.renderJvmTreeSitterClass(
            packageName = packageName,
            className = parserClassName,
            libName = "ktreesitter-$languageName",
            cSymbol = symbol,
          ),
        )
        androidDir.resolve("$parserClassName.kt").writeText(
          io.github.mataku.compose.highlight.buildlogic.renderAndroidTreeSitterClass(
            packageName = packageName,
            className = parserClassName,
            libName = "ktreesitter-$languageName",
            cSymbol = symbol,
          ),
        )
        iosDir.resolve("$parserClassName.kt").writeText(
          io.github.mataku.compose.highlight.buildlogic.renderNativeTreeSitterClass(
            packageName = packageName,
            className = parserClassName,
            cinteropPackage = cinteropEmittedPackage,
            cSymbol = symbol,
          ),
        )
      }
    }

    val genHighlightsTask = tasks.register("generateHighlightsQuery$nameCapitalized") {
      val srcs = qrys.map { grammarDir.resolve(it) }
      val outFile = layout.buildDirectory.dir("generated/secondary/$name/commonMain/kotlin/$highlightsPkgPath").get().asFile.resolve("HighlightsQuery.kt")
      inputs.files(srcs)
      outputs.file(outFile)
      doLast {
        outFile.parentFile.mkdirs()
        val text = srcs.joinToString(separator = "\n") { it.readText() }
          .replace("$", "\${'$'}")
        outFile.writeText(
          """
            |package io.github.mataku.compose.highlight.$name
            |
            |internal const val HIGHLIGHTS_QUERY: String = ${'"'}${'"'}${'"'}
            |$text${'"'}${'"'}${'"'}
            |
          """.trimMargin(),
        )
      }
    }

    genBindingTask.configure { dependsOn(genParserTask) }
    genKotlinTask.configure { dependsOn(genParserTask) }
    genHighlightsTask.configure { dependsOn(genParserTask) }

    extensions.configure<KotlinMultiplatformExtension>("kotlin") {
      sourceSets.named("commonMain") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/secondary/$name/commonMain/kotlin"))
      }
      sourceSets.named("jvmMain") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/secondary/$name/jvmMain/kotlin"))
      }
      sourceSets.named("androidMain") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/secondary/$name/androidMain/kotlin"))
      }
      sourceSets.named("iosMain") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/secondary/$name/iosMain/kotlin"))
      }
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
      dependsOn(genKotlinTask, genHighlightsTask)
    }
    tasks.matching {
      it.name.endsWith("SourcesJar", ignoreCase = true)
    }.configureEach {
      dependsOn(genKotlinTask, genHighlightsTask)
    }
    tasks.matching {
      it.name.startsWith("configureCMake") || it.name.startsWith("buildCMake") ||
        it.name == "configureHostCMake" || it.name == "buildHostCMake"
    }.configureEach {
      dependsOn(genBindingTask, genParserTask)
    }
  }
}
