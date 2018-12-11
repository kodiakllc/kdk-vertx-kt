import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.11"
}

group = "demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    testCompile("io.mockk:mockk:1.8.13.kotlin13")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testCompile("org.testng:testng:6.14.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}