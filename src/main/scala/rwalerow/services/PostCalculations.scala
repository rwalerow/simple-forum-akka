package rwalerow.services

object PostCalculations {

  def calculateBeforeAndAfter(before: Int, after: Int, limit: Int): (Int, Int) = (before, after) match {
    case (0, 0)               => (before, after)
    case (0, a) if a <= limit => (0, a)
    case (b, 0) if b <= limit => (b, 0)
    case (b, a) =>
      val bigger                        = Math.max(b, a).toDouble
      val (firstPartial, secondPartial) = (b / bigger, a / bigger)
      (
        Math.round((limit - 1) * (firstPartial / (firstPartial + secondPartial))).toInt,
        Math.round((limit - 1) * (secondPartial / (firstPartial + secondPartial))).toInt
      )
  }
}
