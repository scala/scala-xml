package scala.xml
package dtd

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait DeclGen extends ArbitraryDecl
    with ContentModelGen
    with ExternalIDGen
    with XmlNameGen
    with Utf8StringGen {

  val genDefault: Gen[DefaultDecl] = for {
    fixed <- Arbitrary.arbitrary[Boolean]
    attValue <- genUtf8String: Gen[String]
  } yield {
    DEFAULT(fixed, attValue)
  }

  val genDefaultDecl: Gen[DefaultDecl] =
    Gen.oneOf(Gen.const(REQUIRED), Gen.const(IMPLIED), genDefault)

  val genElemDecl: Gen[ElemDecl] = for {
    name <- genXmlName: Gen[String]
    contentModel <- Arbitrary.arbitrary[ContentModel]
  } yield {
    ElemDecl(name, contentModel)
  }

  val genAttrDecl: Gen[AttrDecl] = for {
    name <- genXmlName: Gen[String]
    tpe <- genUtf8String: Gen[String]
    default <- Arbitrary.arbitrary[DefaultDecl]
  } yield {
    AttrDecl(name, s"'$tpe'", default)
  }

  val genAttListDecl: Gen[AttListDecl] = for {
    name <- genXmlName: Gen[String]
    attrs <- Arbitrary.arbitrary[List[AttrDecl]].suchThat(_.nonEmpty)
  } yield {
    AttListDecl(name, attrs)
  }

  val genParsedEntityDecl: Gen[ParsedEntityDecl] = for {
    name <- genXmlName: Gen[String]
    entdef <- Arbitrary.arbitrary[EntityDef]
  } yield {
    ParsedEntityDecl(name, entdef)
  }

  def genParameterEntityDecl: Gen[ParameterEntityDecl] = for {
    name <- genXmlName: Gen[String]
    entdef <- Arbitrary.arbitrary[EntityDef]
  } yield {
    ParameterEntityDecl(name, entdef)
  }

  val genUnparsedEntityDecl: Gen[UnparsedEntityDecl] = for {
    name <- genXmlName: Gen[String]
    entdef <- Arbitrary.arbitrary[EntityDef]
    extdef <- Arbitrary.arbitrary[ExternalID]
    notation <- genUtf8String: Gen[String] if notation.nonEmpty
  } yield {
    UnparsedEntityDecl(name, extdef, notation)
  }

  def genEntityDecl: Gen[EntityDecl] =
    Gen.oneOf(
      Arbitrary.arbitrary[ParsedEntityDecl],
      Arbitrary.arbitrary[ParameterEntityDecl],
      Arbitrary.arbitrary[UnparsedEntityDecl]
    )

  val genNotationDecl: Gen[NotationDecl] = for {
    name <- genXmlName: Gen[String]
    extdef <- Arbitrary.arbitrary[ExternalID]
  } yield {
    NotationDecl(name, extdef)
  }

  val genIntDef: Gen[IntDef] = for {
    value <- genXmlName: Gen[String]
  } yield {
    IntDef(value)
  }

  val genExtDef: Gen[ExtDef] = for {
    extID <- Arbitrary.arbitrary[ExternalID]
  } yield {
    ExtDef(extID)
  }

  val genEntityDef: Gen[EntityDef] =
    Gen.oneOf(genIntDef, genExtDef)

  val genPEReference: Gen[PEReference] = for {
    name <- genXmlName: Gen[String]
  } yield {
    PEReference(name)
  }

  // FIXME: SAXParseException: The markup declarations contained or
  // pointed to by the document type declaration must be well-formed.
  val genDecl: Gen[Decl] =
    genElemDecl
  // Gen.oneOf(
  //   genElemDecl,
  //   genAttListDecl,
  //   genEntityDecl
  // )

  implicit val arbDecl = Arbitrary {
    genDecl
  }

  implicit val arbAttListDecl = Arbitrary {
    genAttListDecl
  }

  implicit val arbParsedEntityDecl = Arbitrary {
    genParsedEntityDecl
  }

  implicit val arbParameterEntityDecl = Arbitrary {
    genParameterEntityDecl
  }

  implicit val arbUnparsedEntityDecl = Arbitrary {
    genUnparsedEntityDecl
  }

  implicit val arbAttrDecl = Arbitrary {
    genAttrDecl
  }

  implicit val arbElemDecl = Arbitrary {
    genElemDecl
  }

  implicit val arbEntityDecl = Arbitrary {
    genEntityDecl
  }

  implicit val arbNotationDecl = Arbitrary {
    genNotationDecl
  }

  implicit val arbDefaultDecl = Arbitrary {
    genDefaultDecl
  }

  implicit val arbEntityDef = Arbitrary {
    genEntityDef
  }

  implicit val arbIntDef = Arbitrary {
    genIntDef
  }

  implicit val arbExtDef = Arbitrary {
    genExtDef
  }

  implicit val arbPEReference = Arbitrary {
    genPEReference
  }
}
