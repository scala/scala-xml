package scala.xml
package dtd

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait ArbitraryDecl {

  val genDecl: Gen[Decl]

  implicit val arbDecl: Arbitrary[Decl]

  val genNotationDecl: Gen[NotationDecl]

  implicit val arbNotationDecl: Arbitrary[NotationDecl]
}
