val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val slf4jVersion: String by project

plugins {
    kotlin("jvm")
    java
    id("io.ktor.plugin")
    id("com.github.johnrengelman.shadow") version "7.1.0" // Check for the latest version
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-serialization-gson-jvm")
    implementation("org.jetbrains.exposed", "exposed-core", "0.44.0")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.44.0")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.44.0")
    implementation("org.postgresql:postgresql:42.3.3")
    implementation(project(mapOf("path" to ":models")))
    implementation("com.google.cloud.sql:postgres-socket-factory:1.4.0")
    implementation("com.aallam.openai:openai-client:3.5.1")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")

//    implementation("org.xerial:sqlite-jdbc:3.39.3.0")

    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("com.h2database:h2:1.4.200")

}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}


// Other configurations such as repositories, dependencies, etc.

tasks.test {
    forkEvery = 1
}

tasks.shadowJar {
    archiveBaseName.set("server")
    archiveClassifier.set("") // leave blank to have it be the default JAR
    archiveVersion.set(version.toString())
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
}