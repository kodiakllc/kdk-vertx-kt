package dev.kdk.twodata.vertx

import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch
import mu.KotlinLogging

abstract class BaseCoroutineVerticle<T> : CoroutineVerticle() {
    protected val logger = KotlinLogging.logger(this::class.java.name)

    override suspend fun start() {
        vertx.eventBus().consumer<T>(getAddress()) { message ->
            launch {
                handle(message)
            }
        }
        logger.info { "${this::class.simpleName} started and listening on /${getAddress()}" }
    }

    abstract fun getAddress(): String

    abstract suspend fun handle(message: Message<T>)
}