pluginManagement {
    val `kotlin-version`: String by settings
    val `spring-boot-version`: String by settings
    val `spring-deps-version`: String by settings

    plugins {
        kotlin("jvm") version `kotlin-version`
        kotlin("kapt") version `kotlin-version`
        kotlin("plugin.jpa") version `kotlin-version`
        kotlin("plugin.spring") version `kotlin-version`
        id("org.springframework.boot") version `spring-boot-version`
        id("io.spring.dependency-management") version `spring-deps-version`
    }
}

rootProject.name = "spring-testcontainers-sandbox"
