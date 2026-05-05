plugins {
    kotlin("jvm")
}

group = "org.dynadoc"
version = "1.0.0"

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(project(":alter-annotation"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.7")
    implementation("com.squareup:kotlinpoet:2.3.0")
    implementation("com.squareup:kotlinpoet-ksp:2.3.0")
}
