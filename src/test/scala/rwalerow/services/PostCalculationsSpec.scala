package rwalerow.services

import org.scalatest.{Matchers, WordSpec}
import rwalerow.services.PostCalculations._

class PostCalculationsSpec extends WordSpec with Matchers {

  "PostCalculationsSpec for calculate before and after" should {

    "calculateBeforeAndAfter for 0,0" in {
      calculateBeforeAndAfter(0, 0, 10) shouldEqual (0,0)
    }

    "calculateBeforeAndAfter return second on first 0" in {
      calculateBeforeAndAfter(0, 5, 10) shouldEqual (0, 5)
    }

    "calculateBeforeAndAfter return first on second 0" in {
      calculateBeforeAndAfter(5, 0, 10) shouldEqual (5, 0)
    }

    "calculateBeforeAndAfter return (16, 33) on 50, 100 and limit 50" in {
      calculateBeforeAndAfter(50, 100, 50) shouldEqual (16, 33)
    }
  }
}