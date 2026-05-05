plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "org.dynadoc"
version = "1.0.0"

kotlin {
    jvmToolchain(25)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:6.1.11")
    testImplementation("io.kotest:kotest-assertions-core-jvm:6.1.11")

    kspTest(project(":alter-processor"))
}

tasks.test {
    useJUnitPlatform()
}
