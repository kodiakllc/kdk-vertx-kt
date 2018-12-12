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
    implementation("io.github.microutils:kotlin-logging:1.6.22")
    implementation("ch.qos.logback:logback-classic:1.0.13")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    implementation("io.vertx:vertx-web:3.6.0")
    implementation("io.vertx:vertx-lang-kotlin:3.6.0")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:3.6.0")
    testImplementation("io.vertx:vertx-junit5:3.6.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.2.0")
    testCompile("io.mockk:mockk:1.8.13.kotlin13")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testCompile("org.testng:testng:6.14.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}