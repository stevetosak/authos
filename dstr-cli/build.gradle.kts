
val ktor_version: String by project
plugins {
    kotlin("jvm") version "2.1.20"
    application
}



group = "com.tosak.authos.duster"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.tosak.authos.duster.MainKt")
}
dependencies {
    implementation("io.ktor:ktor-client-jetty:3.1.3")
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-jetty-jakarta:${ktor_version}")
    implementation("io.ktor:ktor-client-apache5:${ktor_version}")
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation("io.ktor:ktor-serialization-jackson:${ktor_version}")
    implementation("io.ktor:ktor-client-core:${ktor_version}")
    implementation("io.ktor:ktor-client-cio:${ktor_version}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktor_version}")
    implementation("io.ktor:ktor-client-logging:${ktor_version}")
}



tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}