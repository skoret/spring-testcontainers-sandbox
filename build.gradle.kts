import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.jpa") // https://kotlinlang.org/docs/no-arg-plugin.html#jpa-support
    kotlin("plugin.spring") // https://kotlinlang.org/docs/all-open-plugin.html#spring-support
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations {
    all {
        // logging framework configuration — slf4j with log4j2
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        // testing framework configuration — junit5 with spring mockk
        exclude(group = "org.mockito")
    }
}

val `kotlin-logs-version`: String by project
val `kotlin-jackson-version`: String by project
val `spring-mockk-version`: String by project
val `testcontainers-version`: String by project

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // core logic dependencies
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // rest dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$`kotlin-jackson-version`")

    // database dependencies
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    // logging framework dependencies — slf4j with log4j2
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("io.github.microutils:kotlin-logging-jvm:$`kotlin-logs-version`")

    // handy spring boot feature for mapping and smart navigation between
    // application properties and corresponding kotlin data classes
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // testing framework dependencies — junit5 with spring mockk
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.ninja-squad:springmockk:$`spring-mockk-version`")

    // testcontainers dependencies
    // use testcontainers to run specific environment in docker container right from tests
    testImplementation("org.testcontainers:testcontainers:$`testcontainers-version`")
    testImplementation("org.testcontainers:junit-jupiter:$`testcontainers-version`")
    testImplementation("org.testcontainers:postgresql:$`testcontainers-version`")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "15"
    }
}
