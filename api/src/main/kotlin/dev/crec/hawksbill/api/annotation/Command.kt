package dev.crec.hawksbill.api.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Command(
    val name: String,
    val description: String,
    val usage: String,
)
