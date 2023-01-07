import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val kotlinVersion = "1.8.0"

    kotlin("jvm") version (kotlinVersion)
    kotlin("plugin.serialization") version (kotlinVersion)

    id("com.github.johnrengelman.shadow") version ("7.1.1")
    application
}

val projectName = "HawksBill"
val botVersion = "1.0.0"

application.mainClass.set("dev.crec.hawksbill.MainKt")

repositories {
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))

    implementation(libs.coroutines)

    implementation(libs.jda)
    implementation(libs.jda.ktx)

    implementation(libs.logback)

    implementation(libs.kmongo)

    implementation(libs.okhttp)
    implementation(libs.classgraph)
    implementation(libs.tomlkt)
    implementation(libs.mathparser)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
    exclude("**/modules/rcon")
    exclude("**/modules/chatbridge")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("$projectName.jar")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
