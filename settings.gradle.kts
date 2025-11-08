enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://nexus.inductiveautomation.com/repository/public")
    }
}

rootProject.name = "ignition-extensions"

includeBuild("build-logic")
include(
    "common",
    "gateway",
    "designer",
    "client",
)

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
