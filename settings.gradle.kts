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

include(":composeApp")
include(":core-api")
include(":core")
include(":languages:kotlin")
include(":languages:swift")
include(":languages:ruby")
include(":languages:rust")
include(":languages:python")
include(":languages:go")
include(":languages:java")
include(":benchmark")

