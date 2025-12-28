plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
}

group = "xyz.rafi67000.kotlinx.gson"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
    implementation("com.google.code.gson:gson:2.13.1")
}

tasks.test {
    useJUnitPlatform()
}