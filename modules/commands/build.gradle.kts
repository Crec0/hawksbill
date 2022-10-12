plugins {
    common
    kotlin("plugin.serialization") version "1.7.10"
}

dependencies {
    api(project(":api"))
    // Calculator
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.0.7")
}
