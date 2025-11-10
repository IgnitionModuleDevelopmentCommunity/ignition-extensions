plugins {
    id("imdc.build.ignition-module-scope")
    id("imdc.test.junit-tests")
}

dependencies {
    compileOnly(libs.ignition.common)
    testImplementation(libs.ignition.common)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk)
}
