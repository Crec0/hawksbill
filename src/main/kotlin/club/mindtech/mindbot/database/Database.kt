package club.mindtech.mindbot.database

import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo

fun initDatabase(dbURI: String, dbName: String): MongoDatabase {
    return KMongo.createClient(dbURI).getDatabase(dbName)
}
