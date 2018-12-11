import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.testng.annotations.Test

enum class Direction {
  north
}

class Car {
  fun drive(direction: Direction): Boolean {
    println("driving $direction")
    return true
  }
}

class CheckTestNgTest {
  private val car: Car = mockk()

  @Test
  fun testCheck() {
    every { car.drive(Direction.north) } returns true
    car.drive(Direction.north) // returns OK
    verify { car.drive(Direction.north) }
  }
}