plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "de.atennert"

kotlin {
    jvm()
    linuxX64("native")
}