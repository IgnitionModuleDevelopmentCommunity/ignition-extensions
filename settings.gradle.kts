enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://nexus.inductiveautomation.com/repository/public")
    }
}

rootProject.name = "ignition-extensions"

include(
    "common",
    "gateway",
    "designer",
    "client",
)
