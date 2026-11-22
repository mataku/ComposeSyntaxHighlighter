rootProject.name = "build-logic"

dependencyResolutionManagement {
  repositories {
    google {
      content {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral {
      content {
        excludeGroupAndSubgroups("androidx")
        excludeGroupAndSubgroups("com.android")
      }
    }
    // Unfiltered, Renovate queries this for every catalog library and the portal redirects non-plugin artifacts to Maven Central, bypassing the Maven Central throttle in renovate.json5.
    // Only the ktreesitter plugin is missing from Maven Central and Google.
    gradlePluginPortal {
      content {
        includeModule("io.github.tree-sitter.ktreesitter-plugin", "io.github.tree-sitter.ktreesitter-plugin.gradle.plugin")
        includeModule("io.github.tree-sitter", "ktreesitter-plugin")
      }
    }
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
