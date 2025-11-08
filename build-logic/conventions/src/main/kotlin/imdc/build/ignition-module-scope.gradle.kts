package imdc.build

import libs
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    id("imdc.build.base")
    alias(libs.plugins.kotlin)
    `java-library`

    alias(libs.plugins.spotless)
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

spotless {
    ratchetFrom("HEAD")
    format("misc") {
        target("*.gradle", ".gitattributes", ".gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
    java {
        palantirJavaFormat()
        formatAnnotations()
    }
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}
