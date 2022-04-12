package club.mindtech.mindbot.database.entities;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Objects;
import java.util.TimeZone;

public class MemberTimeZone {
	@BsonId
	@BsonProperty("member_id")
	private int snowflake;
	private TimeZone timezone;

	public MemberTimeZone(){
	}

	public MemberTimeZone(int snowflake, TimeZone timezone) {
		this.snowflake = snowflake;
		this.timezone = timezone;
	}

	public int getSnowflake() {
		return snowflake;
	}

	public MemberTimeZone setSnowflake(int snowflake) {
		this.snowflake = snowflake;
		return this;
	}

	public TimeZone getTimezone() {
		return timezone;
	}

	public MemberTimeZone setTimezone(TimeZone timezone) {
		this.timezone = timezone;
		return this;
	}

	@Override
	public String toString() {
		return "MemberTimeZone{" + "snowflake=" + snowflake + ", timezone=" + timezone + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MemberTimeZone that)) return false;
		return getSnowflake() == that.getSnowflake() && getTimezone().equals(that.getTimezone());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSnowflake(), getTimezone());
	}
}
