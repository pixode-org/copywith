plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "com.alter.sample"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":alter-annotation"))
    ksp(project(":alter-processor"))
}
