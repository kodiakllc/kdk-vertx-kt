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

class CreateVerticle : AbstractVerticle() {

    private lateinit var mongoClient: MongoClient

    override fun start(startPromise: Promise<Void>) {
        val mongoConfig = config().getJsonObject("mongo")
        mongoClient = MongoClient.createShared(vertx, JsonObject()
            .put("connection_string", mongoConfig.getString("connectionString"))
            .put("db_name", mongoConfig.getString("dbName"))
        )

        val router = Router.router(vertx)

        router.route().handler(BodyHandler.create())
        router.post("/set").handler { ctx -> setDocument(ctx) }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getJsonObject("http").getInteger("setPort")) { http ->
                if (http.succeeded()) {
                    println("HTTP server started on port ${config().getJsonObject("http").getInteger("setPort")}")
                    startPromise.complete()
                } else {
                    println("Failed to start HTTP server: ${http.cause()}")
                    startPromise.fail(http.cause())
                }
            }
    }

    private fun setDocument(ctx: RoutingContext) {
        GlobalScope.launch(vertx.dispatcher()) {
            try {
                val document = ctx.bodyAsJson
                if (document == null) {
                    ctx.response().setStatusCode(400).end("Invalid document")
                    return@launch
                }
                mongoClient.save("mycollection", document).await()
                ctx.response().putHeader("content-type", "text/plain").end("Document inserted")
                println("Document inserted: ${document.encodePrettily()}")
            } catch (e: Exception) {
                println("Failed to insert document: ${e.message}")
                ctx.response().setStatusCode(500).end("Failed to insert document")
            }
        }
    }
}