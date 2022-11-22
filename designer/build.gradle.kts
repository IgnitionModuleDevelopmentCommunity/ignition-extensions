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
    compileOnly(libs.bundles.designer)
    compileOnly(projects.common)
}
