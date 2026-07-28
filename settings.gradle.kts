pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// kh.com.nexgen:base-bom/base-ui/base-navigation/base-database are built from source
// straight out of the BaseProject git repo (private) instead of a Maven repository.
// Gradle clones it once and substitutes these coordinates with the matching subproject.
// Cloning a private repo needs git credentials available to the `git` binary Gradle
// shells out to (e.g. a cached credential helper locally, or a CI step that configures
// one via BASEPROJECT_PAT).
sourceControl {
    gitRepository(uri("https://github.com/khysokhorn/BaseProject.git")) {
        producesModule("kh.com.nexgen:base-bom")
        producesModule("kh.com.nexgen:base-ui")
        producesModule("kh.com.nexgen:base-navigation")
        producesModule("kh.com.nexgen:base-database")
    }
}

rootProject.name = "NexExpend"
include(":app")
 