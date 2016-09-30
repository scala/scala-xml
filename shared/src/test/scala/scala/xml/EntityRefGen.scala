package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait EntityRefGen extends XmlNameGen {

  val genEntityRef: Gen[EntityRef] = for {
    // FIXME: Throws SAXParseException: The entity "{0}" was
    // referenced, but not declared.
    // s <- genXmlName // Would need a corresponding <!ENTITY ...>
    //                 // declaration for `s' to be well-formed.
    // Instead, only generate the pre-defined ones.
    s <- Gen.oneOf("quot", "amp", "apos", "lt", "gt")
  } yield {
    new EntityRef(s)
  }

  implicit val arbEntityRef = Arbitrary {
    genEntityRef
  }
}
