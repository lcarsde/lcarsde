
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "de.atennert"

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        desktopMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)

            implementation("net.java.dev.jna:jna-platform:5.14.0")

            implementation(project(":lcarsde-compose"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "de.atennert.lcarsde.logout.MainKt"

        nativeDistributions {
//            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "logout"
            packageVersion = project.version as String
        }
    }
}