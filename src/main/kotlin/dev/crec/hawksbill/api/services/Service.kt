package dev.crec.hawksbill.api.services

interface Service {
    suspend fun task() {
        TODO("${this.javaClass.simpleName} must implement task method.")
    }

    suspend fun start() = task()
}
