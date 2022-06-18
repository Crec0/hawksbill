import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.1"
    application
}

val projectName = "HawksBill"
val botVersion = "1.0.0"
val group = "dev.crec"

repositories {
    mavenCentral()
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // JDA
    implementation("net.dv8tion:JDA:5.0.0-alpha.12")
    implementation("com.github.minndevelopment:jda-ktx:78e74bc45b8d73a5d7974ef0d5f8efdd5d97910f")

    // Database
    implementation("org.litote.kmongo:kmongo-serialization:4.5.1")

    // Serialization
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.7.0")

    // Logging
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("ch.qos.logback:logback-core:1.2.11")

    // calculator
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.0.2")
}

application.apply {
    mainClass.set("dev.crec.hawksbill.HawksBillKt")
}

tasks.compileKotlin {
    println("Deleting old classes")
    delete("build")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("$projectName.jar")
}
