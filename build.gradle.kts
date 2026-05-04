plugins {
    kotlin("jvm") version "2.3.21" apply false
    id("com.google.devtools.ksp") version "2.3.7" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}
