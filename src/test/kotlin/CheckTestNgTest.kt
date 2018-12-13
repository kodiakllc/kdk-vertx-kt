import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

class Bus {
  suspend fun drive(direction: Direction): Boolean {
    delay(1000)
    println("driving $direction")
    return true
  }
}

class CheckBusTestNgTest {
  private val bus: Bus = mockk()

  @Test
  fun testCheck() {
    coEvery { bus.drive(Direction.north) } returns true
    runBlocking { bus.drive(Direction.north) }
    coVerify { bus.drive(Direction.north) }
  }
}