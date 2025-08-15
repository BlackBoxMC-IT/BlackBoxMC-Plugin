plugins {
    kotlin("jvm") version "2.2.20-Beta2" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
    }
}

