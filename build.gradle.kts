import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.6.20"
    kotlin("plugin.serialization") version "1.6.20"
    id("com.github.johnrengelman.shadow") version "7.1.1"
    application
}

val projectName = "MindBot"
val botVersion = "1.0.0"
val group = "club.mindtech"

repositories {
    mavenCentral()
    maven {
        name = "JDA"
        url = uri("https://m2.dv8tion.net/releases")
    }
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // JDA
    implementation("net.dv8tion:JDA:5.0.0-alpha.10")
    implementation("com.github.minndevelopment:jda-ktx:7c1f33a")

    // Database
    implementation("org.litote.kmongo:kmongo-serialization:4.5.1")

    // Serialization
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.6.20")

    // Logging
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")
}

application.apply {
    mainClass.set("club.mindtech.mindbot.MindBot")
}

tasks.compileKotlin {
    println("Deleting old classes")
    delete("build/classes")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("$projectName-$botVersion.jar")
    minimize()
}
