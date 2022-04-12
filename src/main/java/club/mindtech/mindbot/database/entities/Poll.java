package club.mindtech.mindbot.database.entities;


import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Map;
import java.util.Objects;

public class Poll {
	@BsonId
	@BsonProperty("vote_id")
	private String voteId;
	/**
	 * user snowflake -> poll choice
	 */
	private Map<String, String> votes;

	public Poll() {
	}

	public Poll(String voteId) {
		this(voteId, Map.of());
	}

	public Poll(String voteId, Map<String, String> voters) {
		this.voteId = voteId;
		this.votes = voters;
	}

	public String getVoteId() {
		return voteId;
	}

	public Poll setVoteId(String voteId) {
		this.voteId = voteId;
		return this;
	}

	public Map<String, String> getVotes() {
		return votes;
	}

	public Poll setVotes(Map<String, String> votes) {
		this.votes = votes;
		return this;
	}

	@Override
	public String toString() {
		return "Poll{" + "voteId='" + voteId + '\'' + ", votes=" + votes + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Poll poll)) return false;
		return getVoteId().equals(poll.getVoteId()) && getVotes().equals(poll.getVotes());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getVoteId(), getVotes());
	}
}
