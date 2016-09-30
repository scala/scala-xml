package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait PrettyPrinterGen {

  implicit val arbPrettyPrinter = Arbitrary {
    genPrettyPrinter
  }

  val genPrettyPrinter: Gen[PrettyPrinter] = for {
    width <- Gen.posNum[Int]
    step <- Gen.posNum[Int]
  } yield {
    new PrettyPrinter(width, step)
  }
}
