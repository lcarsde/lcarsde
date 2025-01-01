plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "de.atennert"

kotlin {
    jvm()
    linuxX64("native") {
        compilations.getByName("main") {
            val gtk by cinterops.creating
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        nativeMain {}
        jvmMain {
            dependencies {
                implementation(libs.jna.platform)
            }
        }
    }
}
