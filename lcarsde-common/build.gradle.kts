plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotestMultiplatform)
}

group = "de.atennert"

kotlin {
    jvm()
    linuxX64("native")

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.kotest:kotest-assertions-core:5.4.1")
                implementation("io.kotest:kotest-framework-engine:5.4.1")
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}