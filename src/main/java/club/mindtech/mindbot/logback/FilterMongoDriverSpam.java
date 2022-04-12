package club.mindtech.mindbot.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class FilterMongoDriverSpam extends Filter<ILoggingEvent> {

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (event.getLevel() == Level.DEBUG && event.getLoggerName().startsWith("org.mongodb.driver.")) {
			return FilterReply.DENY;
		}
		return FilterReply.NEUTRAL;
	}
}
