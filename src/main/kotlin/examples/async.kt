package examples

import kotlinx.coroutines.*

import java.lang.Math.abs
import kotlin.random.Random

suspend fun main() = coroutineScope {
    val start = System.currentTimeMillis()
    println("${Thread.currentThread()}:Start")
    listOf("A","B","C").map {
        async { getStock(it) }
    }.map {
        println(it.await())
    }
    println("${Thread.currentThread()}" +
            ":Ended after ${(System.currentTimeMillis() - start) / 1000} secs")
}

private suspend fun getStock(stock: String): String {
    println("${Thread.currentThread()}:getStock for $stock")
    delay(1000)
    return "Stock for $stock = ${abs(Random.nextInt())}"
}