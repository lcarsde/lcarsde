import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "de.atennert.lcarsde"

kotlin {
    val linuxTarget = when (System.getProperty("os.name")) {
        "Linux" -> {
            val architecture: String = ByteArrayOutputStream().use { os ->
                project.exec {
                    commandLine("uname", "-m")
                    standardOutput = os
                }
                os.toString().trim()
            }
            when {
                architecture == "x86_64" -> linuxX64()
                architecture.startsWith("arm64") -> linuxArm64("linuxX64")
                architecture.startsWith("aarch64") -> linuxArm64("linuxX64")
                else -> throw GradleException("Host CPU architecture not supported: $architecture.\n" +
                        "If you think, it should work, please:\n" +
                        "1. check your CPU architecture with \"uname -a\",\n" +
                        "2. find the corresponding target in this list: https://kotlinlang.org/docs/mpp-dsl-reference.html#targets,\n" +
                        "3. add a it to the architectures in build.gradle.kts,\n" +
                        "4. run the build and\n" +
                        "5. if it works, please create a ticket with the changes on https://github.com/lcarsde/lcarsde/issues to have it added permanently.")
            }
        }
        else -> throw GradleException("Host OS is not supported.")
    }

    linuxTarget.apply {
        binaries {
            executable {
                entryPoint = "main"

            }
        }
        compilations.getByName("main") {
            val statusbar by cinterops.creating
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(project(":lcarsde-gtk"))
                implementation(project(":lcarsde-common"))
            }
        }
        commonTest {
            dependencies {
                implementation( "org.jetbrains.kotlin:kotlin-test-common")
            }
        }
        linuxX64Main {
            dependencies {
                implementation(libs.kotlinx.coroutines.core.linux)
            }
        }
        linuxX64Test {}
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

    from(file("src/linuxX64Main/resources"))
    from(file("build/bin/linuxX64/releaseExecutable")) {
        into("/usr/bin/")
    }
}
