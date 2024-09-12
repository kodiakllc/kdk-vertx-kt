import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.ext.mongo.MongoClient
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.launch
import io.vertx.kotlin.coroutines.await
import io.vertx.core.eventbus.Message
import org.bson.types.ObjectId

class CreateVerticle : CoroutineVerticle() {

    internal lateinit var mongoClient: MongoClient

    override suspend fun start() {
        val mongoConfig = config.getJsonObject("mongo")
        mongoClient = MongoClient.createShared(vertx, JsonObject()
            .put("connection_string", mongoConfig.getString("connectionString"))
            .put("db_name", mongoConfig.getString("dbName"))
        )

        vertx.eventBus().consumer<JsonObject>(config.getJsonObject("eventBus").getString("createDocumentAddress")) { message ->
            launch {
                handleCreateDocument(message)
            }
        }
    }

    suspend fun handleCreateDocument(message: Message<JsonObject>) {
        try {
            val document = message.body()
            val objectId = ObjectId()
            document.put("_id", JsonObject().put("\$oid", objectId.toHexString()))
            mongoClient.save("mycollection", document).await()
            message.reply(JsonObject().put("status", "success"))
            println("Document inserted: ${document.encodePrettily()}")
        } catch (e: Exception) {
            println("Failed to insert document: ${e.message}")
            message.fail(500, "Failed to insert document: ${e.message}")
        }
    }


}
