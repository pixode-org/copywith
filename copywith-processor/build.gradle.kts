kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(project(":copywith-annotation"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.7")
    implementation("com.squareup:kotlinpoet:2.3.0")
    implementation("com.squareup:kotlinpoet-ksp:2.3.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name = "CopyWith"
                description = ""
                url = "https://github.com/pixode-org/copywith"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        name = "Flavien Charlon"
                        email = "flavien@charlon.org"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/pixode-org/copywith.git"
                    url = "https://github.com/pixode-org/copywith/tree/master"
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("../../publish/${project.name}-${project.version}"))
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
