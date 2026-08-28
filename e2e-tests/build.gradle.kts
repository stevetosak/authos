import java.time.Duration

plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.tosak.authos"
version = "0.0.1"

repositories { mavenCentral() }

kotlin { jvmToolchain(17) }

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.jackson.module.kotlin)
}

// authos-api is a Maven module; build its runnable jar so docker-compose.e2e.yml can bind-mount it.
val authosApiJar = rootDir.resolve("authos-api/target/Authos-1.0.0-alpha.jar")
tasks.register<Exec>("buildAuthosApiJar") {
    group = "build"
    description = "Packages authos-api (skip tests) into target/Authos-1.0.0-alpha.jar"
    workingDir = rootDir.resolve("authos-api")
    commandLine("./mvnw", "-q", "-DskipTests", "package")
    // Without these the task is "up-to-date" whenever the jar merely exists, so a local
    // e2e run silently tests a stale authos-api binary after any source change.
    inputs.dir(rootDir.resolve("authos-api/src"))
    inputs.file(rootDir.resolve("authos-api/pom.xml"))
    outputs.file(authosApiJar)
}

// PKCS12 keystore CryptoConfig.kt expects: PKCS12, alias authos-jwt-sign (RSA >=2048 + self-signed
// cert), alias authos-credentials-encrypt (AES), both under the store password.
val keystoreFile = layout.buildDirectory.file("keystore/keystore.p12")
tasks.register("generateTestKeystore") {
    group = "build"
    description = "Generates the throwaway PKCS12 keystore the e2e authos-api needs"
    val out = keystoreFile
    outputs.file(out)
    onlyIf { !out.get().asFile.exists() }
    doLast {
        val ks = out.get().asFile
        ks.parentFile.mkdirs()
        val keytool = "${System.getProperty("java.home")}/bin/keytool"
        val runs = listOf(
            listOf(
                keytool, "-genkeypair", "-alias", "authos-jwt-sign",
                "-keyalg", "RSA", "-keysize", "2048", "-sigalg", "SHA256withRSA",
                "-validity", "3650", "-dname", "CN=authos-e2e",
                "-storetype", "PKCS12", "-keystore", ks.absolutePath,
                "-storepass", "changeit", "-keypass", "changeit",
            ),
            listOf(
                keytool, "-genseckey", "-alias", "authos-credentials-encrypt",
                "-keyalg", "AES", "-keysize", "256",
                "-storetype", "PKCS12", "-keystore", ks.absolutePath,
                "-storepass", "changeit", "-keypass", "changeit",
            ),
        )
        runs.forEach { cmd ->
            val rc = ProcessBuilder(cmd).redirectErrorStream(true).inheritIO().start().waitFor()
            check(rc == 0) { "keytool failed (exit $rc): ${cmd.joinToString(" ")}" }
        }
    }
}

// Not wired to `check`/`build`: this task owns a docker-compose stack and must be run explicitly.
tasks.named("test") { enabled = false }

tasks.register<Test>("e2eTest") {
    group = "verification"
    description = "End-to-end + API automation tests against a docker-compose Authos stack"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    dependsOn(":duster:buildFatJar", "buildAuthosApiJar", "generateTestKeystore")
    systemProperty("e2e.keystore", keystoreFile.get().asFile.absolutePath)
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    // let a broken stack fail fast rather than hang the whole suite
    timeout.set(Duration.ofMinutes(15))
}
