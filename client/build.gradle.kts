plugins {
    `java-library`
    kotlin("jvm")
}

kotlin {
    jvmToolchain(libs.versions.java.map(String::toInt).get())
}

dependencies {
    compileOnly(libs.bundles.client)
    compileOnly(projects.common)
}
