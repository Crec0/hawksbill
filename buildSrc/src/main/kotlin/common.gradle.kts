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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("net.dv8tion:JDA:5.0.0-alpha.22")
    implementation("com.github.minndevelopment:jda-ktx:fc7d7de58af04e25eb58c0e8b4923621e3179719")

    implementation("ch.qos.logback:logback-classic:1.2.11")

    implementation("org.litote.kmongo:kmongo-serialization:4.7.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(
            JavaLanguageVersion.of(17)
        )
    }
}
