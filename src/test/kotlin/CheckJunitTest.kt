import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class CheckJunitTest {
  private val car: Car = mockk()

  @Test
  fun testCheck() {
    every { car.drive(Direction.north) } returns true
    car.drive(Direction.north) // returns OK
    verify { car.drive(Direction.north) }
  }
}