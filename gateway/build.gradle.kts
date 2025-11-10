plugins {
    id("imdc.build.ignition-module-scope")
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(projects.common)
}
