package scala.xml.quote.internal

import fastparse.all._

private[internal] object Hole {
  // withing private use area
  private val HoleStart = 0xE000.toChar.toString
  private val HoleChar  = 0xE001.toChar.toString

  def encode(i: Int) = HoleStart + HoleChar * i
  val Parser: P[Int] = P( HoleStart ~ HoleChar.rep ).!.map(_.length - 1)
}
