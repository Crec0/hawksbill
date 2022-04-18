package club.mindtech.mindbot.database

import club.mindtech.mindbot.util.env
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider

fun initDatabase(): MongoDatabase {
    val pojoCodecProvider: CodecProvider = PojoCodecProvider.builder()
        .register("club.mindtech.mindbot.database.entities")
        .build()
    val pojoCodecRegistry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(pojoCodecProvider)
    )
    return MongoClients
        .create(env("DB_URL"))
        .getDatabase(env("DB_NAME"))
        .withCodecRegistry(pojoCodecRegistry)
}
