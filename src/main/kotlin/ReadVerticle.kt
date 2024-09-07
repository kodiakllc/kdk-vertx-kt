import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.mongo.MongoClient
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher

class ReadVerticle : AbstractVerticle() {

    private lateinit var mongoClient: MongoClient

    override fun start(startPromise: Promise<Void>) {
        val mongoConfig = config().getJsonObject("mongo")
        mongoClient = MongoClient.createShared(vertx, JsonObject()
            .put("connection_string", mongoConfig.getString("connectionString"))
            .put("db_name", mongoConfig.getString("dbName"))
        )

        val router = Router.router(vertx)
        router.route("/").handler { ctx -> showDatabaseMap(ctx) }
        router.get("/document").handler { ctx -> getDocument(ctx) }
        router.get("/documents").handler { ctx -> listDocuments(ctx) }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getJsonObject("http").getInteger("getPort")) { http ->
                if (http.succeeded()) {
                    println("HTTP server started on port ${config().getJsonObject("http").getInteger("getPort")}")
                    startPromise.complete()
                } else {
                    println("Failed to start HTTP server: ${http.cause()}")
                    startPromise.fail(http.cause())
                }
            }

        val eventBus = vertx.eventBus()
        eventBus.consumer<JsonObject>(config().getJsonObject("eventBus").getString("getDocumentAddress")) { message ->
            GlobalScope.launch(vertx.dispatcher()) {
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

        eventBus.consumer<JsonObject>(config().getJsonObject("eventBus").getString("listDocumentsAddress")) { message ->
            GlobalScope.launch(vertx.dispatcher()) {
                try {
                    val documents = mongoClient.find("mycollection", JsonObject()).await()
                    message.reply(JsonObject().put("documents", documents))
                } catch (e: Exception) {
                    println("Failed to list documents: ${e.message}")
                    message.fail(500, "Failed to list documents")
                }
            }
        }

        eventBus.consumer<String>(config().getJsonObject("eventBus").getString("getDbMapAddress")) { message ->
            GlobalScope.launch(vertx.dispatcher()) {
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

    private fun showDatabaseMap(ctx: RoutingContext) {
        GlobalScope.launch(vertx.dispatcher()) {
            println("Attempting to retrieve database map")
            val response = vertx.eventBus().request<JsonObject>(config().getJsonObject("eventBus").getString("getDbMapAddress"), "")
            response.onSuccess {
                println("Successfully retrieved database map")
                ctx.response().putHeader("content-type", "application/json").end(it.body().encodePrettily())
            }.onFailure {
                println("Failed to retrieve database map: ${it.message}")
                ctx.response().setStatusCode(500).end("Failed to get database map")
            }
        }
    }

    private fun getDocument(ctx: RoutingContext) {
        GlobalScope.launch(vertx.dispatcher()) {
            val response = vertx.eventBus().request<JsonObject>(config().getJsonObject("eventBus").getString("getDocumentAddress"), JsonObject())
            response.onSuccess {
                ctx.response().putHeader("content-type", "application/json").end(it.body().encodePrettily())
            }.onFailure {
                ctx.response().setStatusCode(404).end("Document not found")
            }
        }
    }

    private fun listDocuments(ctx: RoutingContext) {
        GlobalScope.launch(vertx.dispatcher()) {
            val response = vertx.eventBus().request<JsonObject>(config().getJsonObject("eventBus").getString("listDocumentsAddress"), JsonObject())
            response.onSuccess {
                ctx.response().putHeader("content-type", "application/json").end(it.body().encodePrettily())
            }.onFailure {
                ctx.response().setStatusCode(500).end("Failed to list documents")
            }
        }
    }
}