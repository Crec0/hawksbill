package club.mindtech.mindbot.database.entities;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Objects;
import java.util.UUID;

public class Whitelist {
	@BsonId
	private UUID uuid;
	@BsonProperty("member_id")
	private String snowflake;

	public Whitelist(){}

	public Whitelist(UUID uuid, String snowflake) {
		this.uuid = uuid;
		this.snowflake = snowflake;
	}

	public UUID getUuid() {
		return uuid;
	}

	public Whitelist setUuid(UUID uuid) {
		this.uuid = uuid;
		return this;
	}

	public String getSnowflake() {
		return snowflake;
	}

	public Whitelist setSnowflake(String snowflake) {
		this.snowflake = snowflake;
		return this;
	}

	@Override
	public String toString() {
		return "Whitelist{" + "uuid=" + uuid + ", snowflake='" + snowflake + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Whitelist whitelist)) return false;
		return getUuid().equals(whitelist.getUuid()) && getSnowflake().equals(whitelist.getSnowflake());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUuid(), getSnowflake());
	}
}
