plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "alter"

include(":alter-annotation")
include(":alter-processor")
include(":alter-sample")
