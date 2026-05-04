plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "org.dynadoc.sample"
version = "1.0.0"

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(project(":alter-annotation"))
    ksp(project(":alter-processor"))
}
