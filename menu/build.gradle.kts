
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
//            implementation(compose.foundation)
//            implementation(compose.material)
//            implementation(compose.ui)
//            implementation(compose.components.resources)
//            implementation(compose.components.uiToolingPreview)
//
//            implementation(compose.desktop.currentOs)
//            implementation(libs.kotlinx.coroutines.swing)
//
//            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

            implementation("net.java.dev.jna:jna-platform:5.14.0")

            implementation(project(":lcarsde-compose"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "de.atennert.lcarsde.menu.MainKt"

        nativeDistributions {
//            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "menu"
            packageVersion = project.version as String
        }
    }
}