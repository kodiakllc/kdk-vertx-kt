import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

fun main() {
    withTimingLogged {
        println("${Thread.currentThread()}:START")
        val env = Env()
        println("${Thread.currentThread()}:Start Env")
        env.start()
        println("${Thread.currentThread()}:Sleep for 5 secs")
        Thread.sleep(1100)
        println("${Thread.currentThread()}:Stop Env")
        env.stop()
        println("${Thread.currentThread()}:END")
    }
}

class Env : CoroutineScope {
    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    fun start() {
        repeat(3) {
            launch { doWork(it) }
        }
    }

    fun stop() {
        job.cancel()
    }
}

fun withTimingLogged(block: () -> Unit) {
    val start = System.currentTimeMillis()
    block.invoke()
    println("${Thread.currentThread()}:DONE after ${(System.currentTimeMillis() - start)} millisecs")
}

suspend fun withTimingLoggedForDoWork(index: Int, block: suspend () -> Unit) {
    val start = System.currentTimeMillis()
    block.invoke()
    println("$index==${Thread.currentThread()}:DONE after ${(System.currentTimeMillis() - start)} millisecs")
}

fun CoroutineScope.doWork(index: Int) {
    launch {
        println("${Thread.currentThread()}:doWork CALLED")
        withTimingLoggedForDoWork(index) {
            delay(1000)
        }
        println("${Thread.currentThread()}:doWork DONE")
    }
}
