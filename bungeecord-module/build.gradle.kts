plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "it.blackboxmc.plugin"
version = "v1.0b1rc"

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.4-SNAPSHOT")
    implementation(kotlin("stdlib"))
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation("org.yaml:snakeyaml:2.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


tasks.shadowJar {
    archiveFileName.set("BlackBoxMC-BungeeCord-v1.0b1rc.jar")
    minimize()
}