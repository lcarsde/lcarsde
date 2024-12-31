
plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

group = "de.atennert.lcarsde"

application {
    mainClass = "de.atennert.lcarsde.menu.MenuKt"
    applicationName = "lcarsde-menu"
}

dependencies {
    implementation("net.java.dev.jna:jna-platform:5.14.0")

    implementation(project(":lcarsde-jvm"))
}

distributions {
    main {
        contents {
            into("usr")
            eachFile {
                when (file.extension) {
                    "jar" -> {
                        path = path.replace("/lib/", "/lib/lcarsde/")
                    }
                    "" -> {
                        filter { line -> line.replace("APP_HOME/lib/", "APP_HOME/lib/lcarsde/") }
                    }
                }
            }
            from("src/main/resources") {
                into("..")
            }
            exclude("**/*.bat")
        }
    }
}

tasks.named<Sync>("installDist") {
    // don't use sub folder with application name
    destinationDir = file("build/install")
}
