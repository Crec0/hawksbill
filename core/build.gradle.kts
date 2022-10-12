import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    common
    application
    id("com.github.johnrengelman.shadow") version "7.1.1"
}

val projectName = "HawksBill"
val botVersion = "1.0.0"

dependencies {
    api(project(":api"))

//    implementation(project(":modules:chatbridge"))
    implementation(project(":modules:commands"))

    // Class graph for command registration
    implementation("io.github.classgraph:classgraph:4.8.149")
    // Yaml - For config
    implementation("com.charleskorn.kaml:kaml:0.49.0")
}

application.apply {
    mainClass.set("dev.crec.hawksbill.MainKt")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("$projectName.jar")
}
