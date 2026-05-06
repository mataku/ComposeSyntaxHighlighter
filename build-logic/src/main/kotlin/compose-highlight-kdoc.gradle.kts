import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    id("org.jetbrains.dokka")
}

pluginManager.withPlugin("com.vanniktech.maven.publish") {
    extensions.configure<MavenPublishBaseExtension> {
        configure(
            KotlinMultiplatform(
                javadocJar = JavadocJar.Dokka("dokkaGenerateModuleHtml"),
                sourcesJar = true,
            ),
        )
    }
}
