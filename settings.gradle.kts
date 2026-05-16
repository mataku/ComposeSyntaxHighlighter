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

// :composeApp, :benchmark, and :languages:* are temporarily excluded during AGP 9
// Stage 1 / Stage 2. They will be re-included in Stage 3 (convention plugin
// migration) and Stage 4 (composeApp / benchmark split). See
// docs/plans/2026-05-16-agp-9-phase1-plan.md Task 2.5.
//
// include(":composeApp")
include(":core-api")
// :material3, :material3-text-field temporarily excluded — re-included by
// Tasks 7 / 8 as each migrates to com.android.kotlin.multiplatform.library.
include(":core")
// include(":material3")
// include(":material3-text-field")
// include(":languages:kotlin")
// include(":languages:swift")
// include(":languages:ruby")
// include(":languages:rust")
// include(":languages:python")
// include(":languages:go")
// include(":languages:java")
// include(":languages:markdown")
// include(":languages:javascript")
// include(":languages:typescript")
// include(":benchmark")

gradle.allprojects {
  configurations.all {
    resolutionStrategy.dependencySubstitution {
      substitute(module("io.github.mataku:compose-syntax-highlight-api"))
        .using(project(":core-api"))
        .because("local development: use the :core-api project, not Maven Central")
    }
  }
}
