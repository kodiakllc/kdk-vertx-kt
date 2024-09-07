package examples

import java.lang.Math.abs
import kotlin.random.Random

fun main() {
    val start = System.currentTimeMillis()
    println("${Thread.currentThread()}:Start")
    listOf("A","B","C").map {
        println(getStock(it))
    }
    println("${Thread.currentThread()}" +
            ":Ended after ${(System.currentTimeMillis() - start) / 1000} secs")
}

private fun getStock(stock: String): String {
    println("${Thread.currentThread()}:getStock for $stock")
    Thread.sleep(1000)
    return "Stock for $stock = ${abs(Random.nextInt())}"
}
