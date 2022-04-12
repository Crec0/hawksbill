package club.mindtech.mindbot.database.entities;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Objects;

public class RemindMe {
	@BsonId
	@BsonProperty("member_id")
	private int snowflake;
	private String message;
	private long timeStamp;

	public RemindMe() {
	}

	public RemindMe(int id, String message, long timeStamp) {
		this.snowflake = id;
		this.message = message;
		this.timeStamp = timeStamp;
	}

	public int getSnowflake() {
		return snowflake;
	}

	public RemindMe setSnowflake(int snowflake) {
		this.snowflake = snowflake;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public RemindMe setMessage(String message) {
		this.message = message;
		return this;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public RemindMe setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
		return this;
	}

	@Override
	public String toString() {
		return "RemindMe{" + "snowflake=" + snowflake + ", message='" + message + '\'' + ", timeStamp=" + timeStamp + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RemindMe remindMe)) return false;
		return getSnowflake() == remindMe.getSnowflake() && getTimeStamp() == remindMe.getTimeStamp() && Objects.equals(getMessage(), remindMe.getMessage());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSnowflake(), getMessage(), getTimeStamp());
	}
}
