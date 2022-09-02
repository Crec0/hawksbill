plugins {
    common
    shadow
    application
}

dependencies {
    // calculator
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.0.6")
}

application.apply {
    mainClass.set("dev.crec.hawksbill.HawksBillKt")
}


