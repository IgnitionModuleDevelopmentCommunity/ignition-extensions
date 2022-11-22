plugins {
    `java-library`
    kotlin("jvm")
}

kotlin {
    jvmToolchain {
        languageVersion.set(libs.versions.java.map(JavaLanguageVersion::of))
    }
}

dependencies {
    compileOnly(libs.bundles.client)
    compileOnly(projects.common)
}
