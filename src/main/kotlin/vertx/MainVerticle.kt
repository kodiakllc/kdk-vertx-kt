package dev.kdk.twodata.vertx

import dev.kdk.twodata.business.AnotherEndpointVerticle
import dev.kdk.twodata.business.LicenseVerticle
import dev.kdk.twodata.vertx.BaseCoroutineVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger("MainVerticle")

class MainVerticle : AbstractVerticle() {

    companion object {
        lateinit var router: Router
            private set
    }

    override fun start(startPromise: Promise<Void>) {
        // Initialize router
        router = Router.router(vertx)

        // Register body handler globally
        router.route().handler(BodyHandler.create())

        // Create the HTTP server
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080) { http ->
                if (http.succeeded()) {
                    startPromise.complete()
                    logger.info { "HTTP server started on port 8080" }
                } else {
                    startPromise.fail(http.cause())
                }
            }

        // List of verticles to deploy
        val verticles = listOf(
            AnotherEndpointVerticle(),
            LicenseVerticle()
        )

        // Deploy endpoint verticles
        deployEndpointVerticles(verticles)
    }

    private fun deployEndpointVerticles(verticles: List<BaseCoroutineVerticle<*>>) = runBlocking {
        verticles.forEach { verticle ->
            vertx.deployVerticle(verticle)
            val address = verticle.getAddress()
            registerRoute(address, "/${address}")
        }
        logger.info { "All endpoint verticles deployed" }
    }

    private fun registerRoute(address: String, path: String) {
        router.route(path).handler { ctx ->
            vertx.eventBus().request<String>(address, "") { reply ->
                if (reply.succeeded()) {
                    ctx.response().end(reply.result().body())
                } else {
                    ctx.fail(reply.cause())
                }
            }
        }
        logger.info { "Route registered for address $address at path $path" }
    }
}