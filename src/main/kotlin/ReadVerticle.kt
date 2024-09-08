import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.ext.mongo.MongoClient
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.launch
import io.vertx.kotlin.coroutines.await

class ReadVerticle : CoroutineVerticle() {

    private lateinit var mongoClient: MongoClient

    override suspend fun start() {
        val mongoConfig = config.getJsonObject("mongo")
        mongoClient = MongoClient.createShared(vertx, JsonObject()
            .put("connection_string", mongoConfig.getString("connectionString"))
            .put("db_name", mongoConfig.getString("dbName"))
        )

        vertx.eventBus().consumer<JsonObject>(config.getJsonObject("eventBus").getString("getDocumentAddress")) { message ->
            launch {
                try {
                    val document = mongoClient.findOne("mycollection", JsonObject(), JsonObject()).await()
                    if (document != null) {
                        message.reply(document)
                    } else {
                        message.fail(404, "Document not found")
                    }
                } catch (e: Exception) {
                    println("Failed to get document: ${e.message}")
                    message.fail(500, "Failed to get document")
                }
            }
        }

        vertx.eventBus().consumer<JsonObject>(config.getJsonObject("eventBus").getString("listDocumentsAddress")) { message ->
            launch {
                try {
                    val documents = mongoClient.find("mycollection", JsonObject()).await()
                    message.reply(JsonObject().put("documents", documents))
                } catch (e: Exception) {
                    println("Failed to list documents: ${e.message}")
                    message.fail(500, "Failed to list documents")
                }
            }
        }

        vertx.eventBus().consumer<String>(config.getJsonObject("eventBus").getString("getDbMapAddress")) { message ->
            launch {
                try {
                    val collections = mongoClient.getCollections().await()
                    val collectionDocs = collections.map { collectionName ->
                        val documents = mongoClient.find(collectionName, JsonObject()).await()
                        JsonObject().put("collection", collectionName).put("documents", documents)
                    }
                    val response = JsonObject().put("collections", collectionDocs)
                    message.reply(response)
                } catch (e: Exception) {
                    println("Failed to get database map: ${e.message}")
                    message.fail(500, "Failed to get database map")
                }
            }
        }
    }
}