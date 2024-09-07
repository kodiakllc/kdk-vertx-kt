import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.mongo.MongoClient
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher

class UpdateVerticle : AbstractVerticle() {

    private lateinit var mongoClient: MongoClient

    override fun start(startPromise: Promise<Void>) {
        val mongoConfig = config().getJsonObject("mongo")
        mongoClient = MongoClient.createShared(vertx, JsonObject()
            .put("connection_string", mongoConfig.getString("connectionString"))
            .put("db_name", mongoConfig.getString("dbName"))
        )

        val router = Router.router(vertx)

        // Add body handler to handle JSON bodies
        router.route().handler(BodyHandler.create())
        router.put("/update").handler { ctx -> updateDocument(ctx) }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getJsonObject("http").getInteger("updatePort")) { http ->
                if (http.succeeded()) {
                    println("HTTP server started on port ${config().getJsonObject("http").getInteger("updatePort")}")
                    startPromise.complete()
                } else {
                    println("Failed to start HTTP server: ${http.cause()}")
                    startPromise.fail(http.cause())
                }
            }

        val eventBus = vertx.eventBus()
        eventBus.consumer<JsonObject>(config().getJsonObject("eventBus").getString("updateDocumentAddress")) { message ->
            GlobalScope.launch(vertx.dispatcher()) {
                try {
                    val body = message.body()
                    val query = JsonObject().put("_id", body.getString("id"))
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
    }

    private fun updateDocument(ctx: RoutingContext) {
        GlobalScope.launch(vertx.dispatcher()) {
            val requestBody = ctx.bodyAsJson
            val response = vertx.eventBus().request<JsonObject>(config().getJsonObject("eventBus").getString("updateDocumentAddress"), requestBody)
            response.onSuccess {
                ctx.response().putHeader("content-type", "application/json").end(it.body().encodePrettily())
            }.onFailure {
                ctx.response().setStatusCode(500).end("Failed to update document")
            }
        }
    }
}