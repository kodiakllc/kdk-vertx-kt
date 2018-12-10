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
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}