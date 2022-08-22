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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // JDA
    implementation("net.dv8tion:JDA:5.0.0-alpha.18")
    implementation("com.github.minndevelopment:jda-ktx:081a17728163d978670757b2122381bbb662e731")

    // Database
    implementation("org.litote.kmongo:kmongo-serialization:4.7.0")

    // Serialization
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.7.0")

    // Logging
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("ch.qos.logback:logback-core:1.2.11")

    // calculator
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.0.6")
}

application.apply {
    mainClass.set("dev.crec.hawksbill.HawksBillKt")
}

//tasks.compileKotlin {
//    println("Deleting old classes")
//    delete("build")
//}

tasks.withType<ShadowJar> {
    archiveFileName.set("$projectName.jar")
}
