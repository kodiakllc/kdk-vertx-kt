package dev.kdk.twodata.business

import dev.kdk.twodata.vertx.BaseCoroutineVerticle
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging

class LicenseVerticle : BaseCoroutineVerticle<String>() {

    override fun getAddress() = "license.v2"

    override suspend fun handle(message: Message<String>) {
        // Simulate some processing delay
        delay(1000)
        message.reply("THE LICENSE")
        logger.info { "Handled /${getAddress()} route with message: ${message.body()}" }
    }
}