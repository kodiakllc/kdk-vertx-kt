import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.ext.mongo.MongoClient
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.launch
import io.vertx.kotlin.coroutines.await
import io.vertx.core.eventbus.Message

class ReadVerticle : CoroutineVerticle() {

    internal lateinit var mongoClient: MongoClient

    override suspend fun start() {
        val mongoConfig = config.getJsonObject("mongo")
        mongoClient = MongoClient.createShared(vertx, JsonObject()
            .put("connection_string", mongoConfig.getString("connectionString"))
            .put("db_name", mongoConfig.getString("dbName"))
        )

        vertx.eventBus().consumer<JsonObject>(config.getJsonObject("eventBus").getString("getDocumentAddress")) { message ->
            launch { handleGetDocument(message) }
        }

        vertx.eventBus().consumer<JsonObject>(config.getJsonObject("eventBus").getString("listDocumentsAddress")) { message ->
            launch { handleListDocuments(message) }
        }

        vertx.eventBus().consumer<String>(config.getJsonObject("eventBus").getString("getDbMapAddress")) { message ->
            launch { handleGetDbMap(message) }
        }
    }

    // Function to handle getting a single document from the MongoDB
    suspend fun handleGetDocument(message: Message<JsonObject>) {
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

    // Function to handle listing all documents from the collection
    suspend fun handleListDocuments(message: Message<JsonObject>) {
        try {
            val documents = mongoClient.find("mycollection", JsonObject()).await()
            message.reply(JsonObject().put("documents", documents))
        } catch (e: Exception) {
            println("Failed to list documents: ${e.message}")
            message.fail(500, "Failed to list documents")
        }
    }

    // Function to get all collections and their documents
    suspend fun handleGetDbMap(message: Message<String>) {
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
