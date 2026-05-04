package io.github.mataku.compose.syntax.buildlogic

import org.gradle.api.Task
import org.gradle.api.specs.Spec

object WasmToolchainAvailableSpec : Spec<Task> {
    override fun isSatisfiedBy(task: Task): Boolean {
        val available = isOnPath("tree-sitter") && (isOnPath("docker") || isOnPath("emcc"))
        if (!available) {
            task.logger.warn(
                "${task.path} skipped: requires tree-sitter CLI plus Docker or Emscripten on PATH",
            )
        }
        return available
    }

    private fun isOnPath(executable: String): Boolean {
        val process = ProcessBuilder("which", executable)
            .redirectErrorStream(true)
            .start()
        process.waitFor()
        return process.exitValue() == 0
    }
}
