package club.mindtech.mindbot.database;

import club.mindtech.mindbot.config.Config;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public class Database {
	public static MongoDatabase initDatabase() {
		CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
			.register("club.mindtech.mindbot.database.entities")
			.build();

		CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
			MongoClientSettings.getDefaultCodecRegistry(),
			CodecRegistries.fromProviders(pojoCodecProvider)
		);

		return MongoClients
			.create(Config.DB_URL)
			.getDatabase(Config.DB_NAME)
			.withCodecRegistry(pojoCodecRegistry);
	}
}
