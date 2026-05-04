plugins {
    kotlin("jvm")
}

group = "com.alter"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":alter-annotation"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.7")
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.squareup:kotlinpoet-ksp:1.18.1")
}
