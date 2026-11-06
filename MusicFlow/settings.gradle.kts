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

rootProject.name = "MusicFlow"
include(
    ":app",
    ":core:common",
    ":core:designsystem",
    ":core:network",
    ":core:database",
    ":core:data",
    ":core:player",
    ":core:navigation",
    ":feature:home",
    ":feature:search",
    ":feature:library",
    ":feature:playlist",
    ":feature:player",
)
