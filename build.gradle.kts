import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.gitlab.arturbosch.detekt") version "1.23.3"
    application
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
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("ch.qos.logback:logback-classic:1.2.10")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    // Vert.x Core
    implementation("io.vertx:vertx-core:4.2.1")
    // Vert.x Web
    implementation("io.vertx:vertx-web:4.2.1")
    // Vert.x Kotlin
    implementation("io.vertx:vertx-lang-kotlin:4.2.1")
    // Vert.x Kotlin Coroutines
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.2.1")
    // Vert.x MongoDB Client
    implementation("io.vertx:vertx-mongo-client:4.2.1")
    // Vert.x Config
    implementation("io.vertx:vertx-config:4.2.1")
    // JUnit
    testImplementation("io.vertx:vertx-junit5:4.2.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    // MockK
    testImplementation("io.mockk:mockk:1.12.0")
    // TestNG
    testImplementation("org.testng:testng:7.7.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

application {
    mainClass.set("MainKt")
}