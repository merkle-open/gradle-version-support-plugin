import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.namics.oss.gradle.version"
description = "Gradle plugin to support version management."

plugins {
    val kotlinVersion = "1.3.31"
    kotlin("jvm") version kotlinVersion
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "0.10.1"
//    `maven-publish`
    `java-gradle-plugin`
    id("de.gliderpilot.semantic-release") version "1.4.0"
    id("com.github.hierynomus.license-base") version "0.15.0"
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

gradlePlugin {
    plugins {
        register("com.namics.oss.gradle.version-support-plugin") {
            id = "com.namics.oss.gradle.version-support-plugin"
            implementationClass = "com.namics.oss.gradle.version.VersionSupportPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/namics/gradle-version-support-plugin"
    vcsUrl = "https://github.com/namics/gradle-version-support-plugin"
    description = project.description
    tags = listOf("version", "release", "vcs")
    (plugins) {
        "com.namics.oss.gradle.version-support-plugin" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Gradle Version Support Plugin"
            description = "Gradle plugin to support version handling"
            tags = listOf("version", "release", "vcs")
            version = project.version.toString()
        }
    }
    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = project.name
        version = project.version.toString()
    }
}

if (!version.toString().endsWith("-SNAPSHOT")){
    tasks.getByName("release"){
        finalizedBy("publishPlugins")
    }
}

tasks.create("licenseHeader"){
    dependsOn("licenseFormatMain", "licenseFormatTest")
}

dependencies {
    compile(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.4.3.201909031940-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.apache:5.4.3.201909031940-r")

}


