import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject

class CentralVerticle : AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        val router = Router.router(vertx)

        // CORS handler
        val allowedHeaders = HashSet<String>()
        allowedHeaders.add("x-requested-with")
        allowedHeaders.add("Access-Control-Allow-Origin")
        allowedHeaders.add("origin")
        allowedHeaders.add("Content-Type")
        allowedHeaders.add("accept")
        val allowedMethods = HashSet<HttpMethod>()
        allowedMethods.add(HttpMethod.GET)
        allowedMethods.add(HttpMethod.POST)
        allowedMethods.add(HttpMethod.OPTIONS)
        allowedMethods.add(HttpMethod.DELETE)
        allowedMethods.add(HttpMethod.PUT)

        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods))

        //body handler to handle JSON bodies
        router.route().handler(BodyHandler.create())

        //routes for CRUD operations
        router.post("/set").handler { ctx -> forwardRequest(ctx, config().getJsonObject("eventBus").getString("createDocumentAddress")) }
        router.get("/document").handler { ctx -> forwardRequest(ctx, config().getJsonObject("eventBus").getString("getDocumentAddress")) }
        router.get("/documents").handler { ctx -> forwardRequest(ctx, config().getJsonObject("eventBus").getString("listDocumentsAddress")) }
        router.put("/update").handler { ctx -> forwardRequest(ctx, config().getJsonObject("eventBus").getString("updateDocumentAddress")) }
        router.delete("/delete").handler { ctx -> forwardRequest(ctx, config().getJsonObject("eventBus").getString("deleteDocumentAddress")) }
        router.route("/").handler { ctx -> forwardRequest(ctx, config().getJsonObject("eventBus").getString("getDbMapAddress")) }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getJsonObject("http").getInteger("port")) { http ->
                if (http.succeeded()) {
                    println("HTTP server started on port ${config().getJsonObject("http").getInteger("port")}")
                    startPromise.complete()
                } else {
                    println("Failed to start HTTP server: ${http.cause()}")
                    startPromise.fail(http.cause())
                }
            }
    }

    private fun forwardRequest(ctx: RoutingContext, address: String) {
        val message = if (ctx.body != null && ctx.body.length() > 0) ctx.bodyAsJson else JsonObject()
        vertx.eventBus().request<JsonObject>(address, message) { reply ->
            if (reply.succeeded()) {
                ctx.response().putHeader("content-type", "application/json").end(reply.result().body().encodePrettily())
            } else {
                ctx.response().setStatusCode(500).end("Failed to handle request: ${reply.cause().message}")
            }
        }
    }
}