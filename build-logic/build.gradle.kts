plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.plugins.kotlinMultiplatform.toGradle())
    implementation(libs.plugins.androidLibrary.toGradle())
    implementation(libs.plugins.ktreesitter.toGradle())
    implementation(libs.plugins.composeMultiplatform.toGradle())
    implementation(libs.plugins.composeCompiler.toGradle())
}

fun org.gradle.plugin.use.PluginDependency.toGradle(): String =
    "$pluginId:$pluginId.gradle.plugin:${version.requiredVersion}"

fun Provider<org.gradle.plugin.use.PluginDependency>.toGradle(): String = get().toGradle()
