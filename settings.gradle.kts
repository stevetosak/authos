plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "authos-stack"
include("duster")
include("dstr-cli")
include("e2e-tests")
