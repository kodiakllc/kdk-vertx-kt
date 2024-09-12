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
    // Coroutines (updated version)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Consider updating to latest stable version
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
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    // Vert.x JUnit 5
    testImplementation("io.vertx:vertx-junit5:4.2.1")
    // MockK
    testImplementation("io.mockk:mockk:1.12.0")
    // Vert.x Unit
    testImplementation("io.vertx:vertx-unit:4.2.1")
    // Web Client for HTTP Testing
    implementation("io.vertx:vertx-web-client:4.2.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}
