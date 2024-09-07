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

class DeleteVerticle : AbstractVerticle() {

    private lateinit var mongoClient: MongoClient

    override fun start(startPromise: Promise<Void>) {
        val mongoConfig = config().getJsonObject("mongo")
        mongoClient = MongoClient.createShared(vertx, JsonObject()
            .put("connection_string", mongoConfig.getString("connectionString"))
            .put("db_name", mongoConfig.getString("dbName"))
        )

        val router = Router.router(vertx)

        router.route().handler(BodyHandler.create())
        router.delete("/delete").handler { ctx -> deleteDocument(ctx) }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getJsonObject("http").getInteger("deletePort")) { http ->
                if (http.succeeded()) {
                    println("HTTP server started on port ${config().getJsonObject("http").getInteger("deletePort")}")
                    startPromise.complete()
                } else {
                    println("Failed to start HTTP server: ${http.cause()}")
                    startPromise.fail(http.cause())
                }
            }

        val eventBus = vertx.eventBus()
        eventBus.consumer<JsonObject>(config().getJsonObject("eventBus").getString("deleteDocumentAddress")) { message ->
            GlobalScope.launch(vertx.dispatcher()) {
                try {
                    val query = JsonObject().put("_id", message.body().getString("id"))
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

    private fun deleteDocument(ctx: RoutingContext) {
        GlobalScope.launch(vertx.dispatcher()) {
            val requestBody = ctx.bodyAsJson
            val response = vertx.eventBus().request<JsonObject>(config().getJsonObject("eventBus").getString("deleteDocumentAddress"), requestBody)
            response.onSuccess {
                ctx.response().putHeader("content-type", "application/json").end(it.body().encodePrettily())
            }.onFailure {
                ctx.response().setStatusCode(500).end("Failed to delete document")
            }
        }
    }
}