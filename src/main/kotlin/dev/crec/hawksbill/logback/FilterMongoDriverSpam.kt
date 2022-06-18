package dev.crec.hawksbill.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import dev.crec.hawksbill.isDevelopment

class FilterMongoDriverSpam : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        return if (event.loggerName.startsWith("org.mongodb.driver.")) {
            FilterReply.DENY
        } else if (isDevelopment()) {
            FilterReply.ACCEPT
        } else {
            FilterReply.NEUTRAL
        }
    }
}
