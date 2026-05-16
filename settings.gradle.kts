rootProject.name = "ComposeSyntaxHighlighter"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
  }
}

// :androidApp and :benchmark-android are NEW modules added in Stage 4 (Tasks 16 / 20).
// See docs/plans/2026-05-16-agp-9-phase1-plan.md.
include(":composeApp")
include(":androidApp") // new in Stage 4 (Task 16)
include(":core-api")
include(":core")
include(":material3")
include(":material3-text-field")
include(":languages:kotlin")
include(":languages:swift")
include(":languages:ruby")
include(":languages:rust")
include(":languages:python")
include(":languages:go")
include(":languages:java")
include(":languages:markdown")
include(":languages:javascript")
include(":languages:typescript")
include(":benchmark") // KMP+JVM only after Task 19
include(":benchmark-android") // new in Stage 4 (Task 20)

gradle.allprojects {
  configurations.all {
    resolutionStrategy.dependencySubstitution {
      substitute(module("io.github.mataku:compose-syntax-highlight-api"))
        .using(project(":core-api"))
        .because("local development: use the :core-api project, not Maven Central")
    }
  }
}
