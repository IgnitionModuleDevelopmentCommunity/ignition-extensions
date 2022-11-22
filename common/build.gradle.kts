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
    compileOnly(libs.ignition.common)
    testImplementation(libs.ignition.common)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}
