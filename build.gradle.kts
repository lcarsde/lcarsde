plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotestMultiplatform) apply false
}

tasks.register<Copy>("combineRelease")

tasks.named<Copy>("combineRelease") {
    description = "Copies all builds into release"
    group = "distribution"

    val projDir = layout.projectDirectory
    from(
        projDir.dir("app-selector/build/install"),
        projDir.dir("lcarswm/build/install"),
        projDir.dir("logout/build/install"),
        projDir.dir("menu/build/install"),
        projDir.dir("status-bar/build/install"),
    )
    into(layout.buildDirectory.dir("release").get().asFile)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}