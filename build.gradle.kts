// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

tasks.register<Copy>("extractBaseDatabaseSources") {
    val sourcesJar = configurations.detachedConfiguration(
        dependencies.create("kh.com.nexgen:base-database:1.0.0:sources")
    ).files.first { it.name.contains("base-database") }
    from(zipTree(sourcesJar))
    into(layout.buildDirectory.dir("extracted-sources"))
}