@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modl)
}

allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://nexus.inductiveautomation.com/repository/public")
    }
}

subprojects {
    // cascade version, which will be set at command line in CI, down to subprojects
    version = rootProject.version
}

ignitionModule {
    name.set("Ignition Extensions")
    fileName.set("Ignition-Extensions.modl")
    id.set("org.imdc.extensions.IgnitionExtensions")
    moduleVersion.set("${project.version}")
    moduleDescription.set("Useful but niche extensions to Ignition for power users")
    license.set("LICENSE.html")
    requiredIgnitionVersion.set(libs.versions.ignition)

    projectScopes.putAll(
        mapOf(
            projects.common.dependencyProject.path to "GDC",
            projects.gateway.dependencyProject.path to "G",
            projects.designer.dependencyProject.path to "D",
            projects.client.dependencyProject.path to "C",
        ),
    )

    hooks.putAll(
        mapOf(
            "org.imdc.extensions.gateway.GatewayHook" to "G",
            "org.imdc.extensions.designer.DesignerHook" to "D",
            "org.imdc.extensions.client.ClientHook" to "C",
        ),
    )

    skipModlSigning.set(!findProperty("signModule").toString().toBoolean())
}

tasks.deployModl {
    hostGateway.set("http://localhost:18088")
}
