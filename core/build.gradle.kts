import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    common
    application
    alias(libs.plugins.shadow)
}

val projectName = "HawksBill"
val botVersion = "1.0.0"

application.mainClass.set("dev.crec.hawksbill.MainKt")

dependencies {
    api(project(":api"))

//    implementation(project(":modules:chatbridge"))
    implementation(project(":modules:commands"))

    implementation(libs.coroutines)
    implementation(libs.stdlib)

    implementation(libs.jda)
    implementation(libs.jda.ktx)

    implementation(libs.logback)

    implementation(libs.kmongo)

    implementation(libs.classgraph)
    implementation(libs.kaml)

}

tasks.withType<ShadowJar> {
    archiveFileName.set("$projectName.jar")
}
