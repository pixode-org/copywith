import org.jetbrains.kotlin.gradle.dsl.JvmTarget

allprojects {
    group = "org.pixode"
    version = "1.0.0"
}

plugins {
    kotlin("jvm") version "2.3.21"
    id("com.google.devtools.ksp") version "2.3.7" apply false
    id("maven-publish")
    id("signing")
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    // Apply the java-library plugin for API and implementation separation.
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
    }

    tasks.named<Test>("test") {
        // Use JUnit Platform for unit tests.
        useJUnitPlatform()
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}
