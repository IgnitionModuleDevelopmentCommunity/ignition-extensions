package imdc.build

import libs
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    id("imdc.build.base")
    alias(libs.plugins.kotlin)
    `java-library`
}

version = project.parent?.version ?: "0.0.0-SNAPSHOT"

val jvmLanguageVersion = libs.versions.java.map { JavaLanguageVersion.of(it) }

configure<KotlinProjectExtension> {
    jvmToolchain {
        languageVersion = jvmLanguageVersion
    }
}

configure<JavaPluginExtension> {
    toolchain {
        languageVersion = jvmLanguageVersion
    }
}
