plugins {
    id("imdc.build.ignition-module-scope")
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(projects.common)
}
