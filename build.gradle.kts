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
    val installDependencies = gradle.rootProject.subprojects
        .filter { it.tasks.any { t -> t.name == "installDist" } }
        .map { ":${it.name}:installDist" }
    dependsOn(installDependencies)

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

tasks.register<Delete>("clean")
tasks.named<Delete>("clean") {
    group = "build"
    description = "Delete build directories"

    val cleanDependencies = gradle.rootProject.subprojects
        .filter { it.tasks.any { t -> t.name == "clean" } }
        .map { ":${it.name}:clean" }
    shouldRunAfter(cleanDependencies)

    delete(layout.buildDirectory)
}
