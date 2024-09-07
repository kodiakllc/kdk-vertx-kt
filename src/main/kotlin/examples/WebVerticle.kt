package examples

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger("WebVerticle")

suspend fun main() = coroutineScope {
    deploy()
}

fun CoroutineScope.deploy() {
    launch {
        Vertx.vertx().deployVerticle(WebVerticle())
        logger.info { "Deployed WebVerticle" }
    }
    Thread.sleep(5000)
}

class WebVerticle : CoroutineVerticle() {
    override suspend fun start() {
        val router = Router.router(vertx)
        router.route("/").handler { ctx -> getLicense(ctx) }
        vertx.createHttpServer().requestHandler(router).listen(8080)
        logger.info { "Listening on port 8080" }
    }

    fun CoroutineScope.getLicense(ctx: RoutingContext) {
        launch {
            logger.info { "${Thread.currentThread()}:getLicense CALLED" }
            delay(1000)
            ctx.response().putHeader("content-type", "text/plain").end("THE LICENSE")
            logger.info { "${Thread.currentThread()}:getLicense DONE" }
        }
    }
}
