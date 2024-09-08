import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.ext.mongo.MongoClient
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.launch
import io.vertx.kotlin.coroutines.await
import org.bson.types.ObjectId

class DeleteVerticle : CoroutineVerticle() {

    private lateinit var mongoClient: MongoClient

    override suspend fun start() {
        val mongoConfig = config.getJsonObject("mongo")
        mongoClient = MongoClient.createShared(vertx, JsonObject()
            .put("connection_string", mongoConfig.getString("connectionString"))
            .put("db_name", mongoConfig.getString("dbName"))
        )

        vertx.eventBus().consumer<JsonObject>(config.getJsonObject("eventBus").getString("deleteDocumentAddress")) { message ->
            launch {
                try {
                    val id = message.body().getString("id")
                    val query = if (ObjectId.isValid(id)) {
                        JsonObject().put("_id", JsonObject().put("\$oid", id))
                    } else {
                        JsonObject().put("_id", id)
                    }
                    println("Delete Query: $query")
                    val result = mongoClient.findOneAndDelete("mycollection", query).await()
                    if (result != null) {
                        message.reply(result)
                    } else {
                        message.fail(404, "Document not found")
                    }
                } catch (e: Exception) {
                    println("Failed to delete document: ${e.message}")
                    message.fail(500, "Failed to delete document")
                }
            }
        }
    }
}