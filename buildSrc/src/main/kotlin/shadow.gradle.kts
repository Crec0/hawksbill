import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("${rootProject.name}.jar")
}
