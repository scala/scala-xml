package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait AtomGen extends PCDataGen
    with TextGen
    with UnparsedGen {

  val genAtom: Gen[Atom[String]] =
    Gen.oneOf(genPCData, genText, genUnparsed)

  implicit val arbAtom = Arbitrary {
    genAtom
  }
}
