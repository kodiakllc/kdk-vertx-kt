package dev.kdk.twodata.business

import dev.kdk.twodata.vertx.BaseCoroutineVerticle
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging

class AnotherEndpointVerticle : BaseCoroutineVerticle<String>() {

    override fun getAddress() = "another.v2"

    override suspend fun handle(message: Message<String>) {
        // Simulate some processing delay
        delay(1000)
        message.reply("ANOTHER RESPONSE")
        logger.info { "Handled /${getAddress()} route with message: ${message.body()}" }
    }
}