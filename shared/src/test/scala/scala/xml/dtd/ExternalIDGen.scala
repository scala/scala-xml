package scala.xml
package dtd

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait ExternalIDGen extends Utf8StringGen {

  // FIXME: Generate any string of chars, not just alphas.  Alphas
  // avoid "Gave up after only 0 passed tests. 501 tests were
  // discarded."
  val genPubIdStr = Gen.alphaStr
    .suchThat(_.nonEmpty).suchThat(Utility.checkPubID(_))

  // FIXME: Generate any string of chars, not just alphas, Alphas
  // avoid "Gave up after only 0 passed tests. 501 tests were
  // discarded."
  val genSysIdStr = Gen.alphaStr
    .suchThat(_.nonEmpty).suchThat(Utility.checkSysID(_))

  val genNonPubIdStr = Arbitrary.arbitrary[String]
    .suchThat(!Utility.checkPubID(_))

  val genNonSysIdStr = Gen.listOf(Gen.oneOf("'", "\""))
    .map(_.mkString).suchThat(!Utility.checkSysID(_))

  def genExternalID: Gen[ExternalID] =
    Gen.oneOf(
      Gen.const(NoExternalID),
      Arbitrary.arbitrary[PublicID],
      Arbitrary.arbitrary[SystemID]
    )

  val genSystemID: Gen[SystemID] = for {
    systemId <- genSysIdStr
  } yield {
    new SystemID(systemId)
  }

  val genPublicID: Gen[PublicID] = for {
    pubId <- genPubIdStr
    systemId <- genSysIdStr
  } yield {
    new PublicID(pubId, systemId)
  }

  implicit val arbSystemID = Arbitrary {
    genSystemID
  }

  implicit val arbPublicID = Arbitrary {
    genPublicID
  }

  implicit val arbExternalID = Arbitrary {
    genExternalID
  }
}
