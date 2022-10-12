package dev.crec.hawksbill.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import dev.crec.hawksbill.isDev

class FilterMongoDriverSpam : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        return if (
            event.loggerName.startsWith("org.mongodb.driver.") ||
            (event.level.toInt() == Level.DEBUG.toInt() && !isDev)
        ) {
            FilterReply.DENY
        } else {
            FilterReply.NEUTRAL
        }
    }
}
