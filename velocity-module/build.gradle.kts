import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "it.blackboxmc.plugin"
version = "v1.0b1rc"

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation(kotlin("stdlib"))
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
    implementation("com.google.inject:guice:5.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.yaml:snakeyaml:2.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


tasks.shadowJar {
    archiveFileName.set("BlackBoxMC-Velocity-v1.0b1rc.jar")
    minimize()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}