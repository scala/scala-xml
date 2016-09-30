package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait DocumentGen extends NodeGen
    with dtd.DocTypeGen
    with dtd.DTDGen {

  val genDocument: Gen[Document] = for {
    prolog <- genProlog
    dtd_ <- Arbitrary.arbitrary[dtd.DTD]
    elements <- Arbitrary.arbitrary[Elem]
    n <- Gen.choose(0, 4)
    misc <- Gen.listOfN(n, genMisc)
  } yield {
    new Document {
      children = prolog
      dtd = dtd_
      docElem = elements
    }
  }

  implicit val arbDocument = Arbitrary {
    genDocument
  }

  def genWhiteSpace: Gen[String] =
    Gen.listOf(Gen.oneOf(' ', '\t', '\n', '\r')).map(_.mkString)

  def genMisc: Gen[Node] =
    Gen.oneOf(
      Arbitrary.arbitrary[Comment],
      Arbitrary.arbitrary[ProcInstr],
      genWhiteSpace.map(Text(_))
    )

  // Seems the intention was to have the Document.children contain the
  // XML "prologue", but it would need to contain XML declarations and
  // DocType declarations, which are are not of type Node, and
  // therefore wouldn't exist in a Seq[Node].  Oh, well.  XML.write
  // ended up avoiding the Document type, and just taking the values
  // it needed as arguments rather than from class fields.
  def genProlog: Gen[NodeSeq] = for {
    // xmlDecl <- genXmlDecl
    n <- Gen.choose(0, 4)
    misc <- Gen.listOfN(n, genMisc)
    // docType <- Arbitrary.arbitrary[dtd.DocType]
    // misc2 <- Gen.listOf(genMisc)
    // prolog2 <- Gen.const(Text(docType.toString) :: misc2)
    // prolog <- Gen.oneOf(
    //   xmlDecl + misc + prolog2,
    //   Gen.const(misc ++ prolog2),
    //   Gen.const(misc)
    // )
  } yield {
    // NodeSeq.fromSeq(prolog)
    NodeSeq.fromSeq(misc)
  }

  val genXmlDecl: Gen[String] =
    Gen.const("<?xml version='1.0' encoding='UTF-8'?>")
}
