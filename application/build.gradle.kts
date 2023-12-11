import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val slf4jVersion: String by project

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

group = "com.example"
version = "1.0-SNAPSHOT"

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(project(":models"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("commons-codec:commons-codec:1.16.0")
    implementation("io.ktor:ktor-client-auth:2.3.5")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "cs346-project-final"
            packageVersion = "1.0.0"
            modules("java.net.http")
        }
    }
}
