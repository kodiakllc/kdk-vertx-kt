import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.ext.mongo.MongoClient
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import io.vertx.core.eventbus.Message
import kotlinx.coroutines.launch
import org.bson.types.ObjectId

class UpdateVerticle : CoroutineVerticle() {

    internal lateinit var mongoClient: MongoClient

    override suspend fun start() {
        val mongoConfig = config.getJsonObject("mongo")
        mongoClient = MongoClient.createShared(vertx, JsonObject()
            .put("connection_string", mongoConfig.getString("connectionString"))
            .put("db_name", mongoConfig.getString("dbName"))
        )

        vertx.eventBus().consumer<JsonObject>(config.getJsonObject("eventBus").getString("updateDocumentAddress")) { message ->
            launch {
                handleUpdateDocument(message)
            }
        }
    }

    suspend fun handleUpdateDocument(message: Message<JsonObject>) {
        try {
            val body = message.body()
            val id = body.getString("id")
            val query = if (ObjectId.isValid(id)) {
                JsonObject().put("_id", JsonObject().put("\$oid", id))
            } else {
                JsonObject().put("_id", id)
            }
            println("Update Query: $query")
            val update = JsonObject().put("\$set", body.getJsonObject("update"))
            val result = mongoClient.findOneAndUpdate("mycollection", query, update).await()
            if (result != null) {
                message.reply(result)
            } else {
                message.fail(404, "Document not found")
            }
        } catch (e: Exception) {
            println("Failed to update document: ${e.message}")
            message.fail(500, "Failed to update document")
        }
    }
}
