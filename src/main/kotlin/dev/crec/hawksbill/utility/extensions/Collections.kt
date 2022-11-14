package dev.crec.hawksbill.utility.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.DelayQueue
import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit

fun <T : Delayed> DelayQueue<T>.asExpiredValuesFlow() = flow {
    while (true) {
        val polledValue = this@asExpiredValuesFlow.poll(50, TimeUnit.MILLISECONDS) ?: break
        emit(polledValue)
    }
}.flowOn(Dispatchers.IO)
