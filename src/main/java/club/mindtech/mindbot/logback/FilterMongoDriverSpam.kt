package club.mindtech.mindbot.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class FilterMongoDriverSpam : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        return if (event.level == Level.DEBUG && event.loggerName.startsWith("org.mongodb.driver.")) {
            FilterReply.DENY
        } else {
            FilterReply.NEUTRAL
        }
    }
}
