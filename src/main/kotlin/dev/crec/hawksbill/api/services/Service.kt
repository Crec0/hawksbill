package dev.crec.hawksbill.api.services

interface Service {
    suspend fun runTask(task: suspend () -> Unit)
}
