plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()

    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(kotlin(module = "serialization"))
    implementation(kotlin(module = "stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("net.dv8tion:JDA:5.0.0-alpha.18")
    implementation("com.github.minndevelopment:jda-ktx:081a17728163d978670757b2122381bbb662e731")

    implementation("ch.qos.logback:logback-classic:1.2.11")

    implementation("org.litote.kmongo:kmongo-serialization:4.7.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

