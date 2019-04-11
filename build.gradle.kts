import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.namics.oss.gradle.version"
description = "Gradle plugin to support version management."

plugins {
    val kotlinVersion = "1.3.20"
    kotlin("jvm") version kotlinVersion
    `kotlin-dsl`
    `java-gradle-plugin`
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.0")
}

repositories {
    mavenCentral()
}

tasks.withType(KotlinCompile::class) {
    kotlinOptions {
        javaParameters = true
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        register("com.namics.oss.gradle.version-support-plugin") {
            id = "com.namics.oss.gradle.version-support-plugin"
            implementationClass = "com.namics.oss.gradle.version.VersionSupportPlugin"
        }
    }
}

