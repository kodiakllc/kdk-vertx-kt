package dev.kdk.twodata

import dev.kdk.twodata.vertx.MainVerticle
import io.vertx.core.Vertx
import mu.KotlinLogging

private val logger = KotlinLogging.logger("MainLauncher")

fun main() {
    val vertx = Vertx.vertx()

    vertx.deployVerticle(MainVerticle()) { res ->
        if (res.succeeded()) {
            logger.info { "MainVerticle deployment succeeded" }
        } else {
            logger.error(res.cause()) { "MainVerticle deployment failed" }
        }
    }
}