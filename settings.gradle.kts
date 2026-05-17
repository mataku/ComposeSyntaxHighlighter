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

include(":samples:composeApp")
include(":samples:androidApp")
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
include(":benchmarks:jvm")
include(":benchmarks:android")

gradle.allprojects {
  configurations.all {
    resolutionStrategy.dependencySubstitution {
      substitute(module("io.github.mataku:compose-syntax-highlight-api"))
        .using(project(":core-api"))
        .because("local development: use the :core-api project, not Maven Central")
    }
  }
}
