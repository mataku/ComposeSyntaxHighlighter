package io.github.mataku.compose.highlight.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class StageJvmNativeLibraryTask : DefaultTask() {

  @get:InputFile
  abstract val builtLibrary: RegularFileProperty

  @get:Input
  abstract val hostOs: Property<String>

  @get:Input
  abstract val hostArch: Property<String>

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @get:Inject
  abstract val execOperations: ExecOperations

  @TaskAction
  fun stage() {
    val source = builtLibrary.get().asFile
    val root = outputDir.get().asFile
    root.deleteRecursively()
    val os = hostOs.get()
    if (os == "macos") {
      MACOS_SLICES.forEach { (resourceArch, lipoArch) ->
        val target = resourceFile(root, os, resourceArch, source.name)
        thinSlice(source, lipoArch, target)
        adHocSign(target)
      }
    } else {
      source.copyTo(resourceFile(root, os, hostArch.get(), source.name), overwrite = true)
    }
  }

  private fun resourceFile(root: File, os: String, arch: String, fileName: String): File =
    root.resolve("lib").resolve(os).resolve(arch).resolve(fileName).also { it.parentFile.mkdirs() }

  private fun thinSlice(source: File, lipoArch: String, target: File) {
    execOperations.exec {
      commandLine("lipo", "-thin", lipoArch, source.absolutePath, "-output", target.absolutePath)
    }
  }

  // The linker ad-hoc signs arm64 output but not x86_64, so signing only the
  // slices that lack a signature would leave the two architectures inconsistent.
  private fun adHocSign(target: File) {
    execOperations.exec {
      commandLine("codesign", "--force", "-s", "-", target.absolutePath)
    }
  }

  private companion object {
    val MACOS_SLICES = listOf("aarch64" to "arm64", "x64" to "x86_64")
  }
}
