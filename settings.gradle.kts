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
include(":core")
include(":languages:kotlin")
include(":languages:swift")
include(":languages:ruby")
include(":languages:rust")

val localPropsFile = rootDir.resolve("local.properties")
if (localPropsFile.exists()) {
    val localProps = java.util.Properties().apply {
        localPropsFile.inputStream().use(::load)
    }
    val publishKeys = listOf(
        "mavenCentralUsername",
        "mavenCentralPassword",
        "signingInMemoryKey",
        "signingInMemoryKeyId",
        "signingInMemoryKeyPassword",
    )
    gradle.beforeProject {
        publishKeys.forEach { key ->
            if (!hasProperty(key)) {
                localProps.getProperty(key)?.let { extensions.extraProperties.set(key, it) }
            }
        }
    }
}