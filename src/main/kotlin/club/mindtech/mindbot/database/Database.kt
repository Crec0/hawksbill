package club.mindtech.mindbot.database

import club.mindtech.mindbot.bot
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

fun initDatabase(dbURI: String, dbName: String): MongoDatabase {
    return KMongo.createClient(dbURI).getDatabase(dbName)
}

inline fun <reified T : Entity> getCollection(): MongoCollection<T> {
    return bot.database.getCollection()
}
