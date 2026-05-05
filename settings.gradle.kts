plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "copywith"

include(":copywith-annotation")
include(":copywith-processor")
