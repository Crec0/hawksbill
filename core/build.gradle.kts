plugins {
    common
    shadow
    application
}

dependencies {
    api(project(":api"))
    implementation(project(":modules:utility"))

    implementation("io.github.classgraph:classgraph:4.8.149")
    // calculator
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.0.6")
}

application.apply {
    mainClass.set("dev.crec.hawksbill.HawksBillKt")
}


