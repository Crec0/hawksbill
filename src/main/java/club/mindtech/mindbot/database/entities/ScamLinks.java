package club.mindtech.mindbot.database.entities;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Objects;

public class ScamLinks {
	@BsonId
	private String link;

	public ScamLinks() {
	}

	public ScamLinks(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public ScamLinks setLink(String link) {
		this.link = link;
		return this;
	}

	@Override
	public String toString() {
		return "ScamLinks{" + "link='" + link + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ScamLinks scamLinks)) return false;
		return getLink().equals(scamLinks.getLink());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getLink());
	}
}
