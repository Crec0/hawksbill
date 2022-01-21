plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version ("7.1.1")
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
}

dependencies {
    implementation("net.dv8tion:JDA:5.+") {
        exclude(module = "opus-java")
    }
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.10")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("${projectName}-${botVersion}.jar")
}

application.apply {
    mainClass.set("${group}.${projectName.toLowerCase()}.${projectName}")
}
