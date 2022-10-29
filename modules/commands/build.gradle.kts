plugins {
    common
}

dependencies {
    api(project(":api"))
    implementation(libs.jda)
    implementation(libs.jda.ktx)

    implementation(libs.kmongo)
    implementation(libs.mathparser)
}
