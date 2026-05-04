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
    testImplementation(project(":alter-annotation"))
    testImplementation(kotlin("test"))

    kspTest(project(":alter-processor"))
}

tasks.test {
    useJUnitPlatform()
}
