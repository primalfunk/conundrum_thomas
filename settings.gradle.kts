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

rootProject.name = "Conundrum Thomas V2"
include(":app")
include(":thomas:domain")
include(":thomas:provenance")
include(":thomas:ontology")
include(":thomas:engine")
include(":thomas:safety")
include(":thomas:longitudinal")
include(":thomas:longitudinal-admission")
include(":thomas:longitudinal-store")
include(":thomas:language-evidence")
include(":thomas:runtime")
include(":platform:persistence-android")
include(":platform:renderer-llama-android")
include(":platform:speech-android")
include(":qualification")
include(":tools:provenance")
