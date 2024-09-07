import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.gitlab.arturbosch.detekt") version "1.23.3"
}

group = "kdk"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("io.github.microutils:kotlin-logging:1.6.22")
    implementation("ch.qos.logback:logback-classic:1.0.13")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    // Vert.x
    implementation("io.vertx:vertx-web:3.9.16")
    implementation("io.vertx:vertx-lang-kotlin:3.9.16")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:3.9.16")
    testImplementation("io.vertx:vertx-junit5:3.9.16")
    // MongoDB
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.1.4")
    // JUnit
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    // MockK
    testImplementation("io.mockk:mockk:1.8.13.kotlin13")
    // TestNG
    testImplementation("org.testng:testng:7.7.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}