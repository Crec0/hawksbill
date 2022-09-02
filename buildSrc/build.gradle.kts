plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin(module = "gradle-plugin"))
    implementation("com.github.johnrengelman.shadow", "com.github.johnrengelman.shadow.gradle.plugin", "7.1.2")
}
