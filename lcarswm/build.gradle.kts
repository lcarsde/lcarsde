import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    kotlin("plugin.serialization") version "2.0.20"
    id("io.kotest.multiplatform") version "5.4.1"
}

group = "de.atennert"

kotlin {
    val nativeTarget = when (System.getProperty("os.name")) {
        "Linux" -> {
            val architecture: String = ByteArrayOutputStream().use { os ->
                project.exec {
                    commandLine("uname", "-m")
                    standardOutput = os
                }
                os.toString().trim()
            }
            when {
                architecture == "x86_64" -> linuxX64("native")
                architecture.startsWith("arm64") -> linuxArm64("native")
                architecture.startsWith("aarch64") -> linuxArm64("native")
                else -> throw GradleException("Host CPU architecture not supported: $architecture.\n" +
                        "If you think, it should work, please:\n" +
                        "1. check your CPU architecture with \"uname -a\",\n" +
                        "2. find the corresponding target in this list: https://kotlinlang.org/docs/mpp-dsl-reference.html#targets,\n" +
                        "3. add a it to the architectures in build.gradle.kts,\n" +
                        "4. run the build and\n" +
                        "5. if it works, please create a ticket with the changes on https://github.com/lcarsde/lcarswm/issues to have it added permanently.")
            }
        }
        else -> throw GradleException("Host OS is not supported.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
        compilations.getByName("main") {
            val xlib by cinterops.creating
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(project(":lcarsde-common"))
            }
        }
        commonTest {
            dependencies {
                implementation("io.kotest:kotest-assertions-core:5.4.1")
                implementation("io.kotest:kotest-framework-engine:5.4.1")
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.register<Copy>("installDist")
tasks.named<Copy>("installDist") {
    group = "distribution"
    dependsOn("build")

    into("build/install")

    from(file("src/nativeMain/resources"))
    from(file("build/bin/native/releaseExecutable")) {
        into("/usr/bin/")
    }
}
