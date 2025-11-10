plugins {
    id("imdc.build.ignition-module-scope")
}

dependencies {
    compileOnly(libs.bundles.client)
    compileOnly(projects.common)
}
