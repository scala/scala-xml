package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait XmlStringGen extends DocumentGen {

  def xmlDeclGen(version: String, encoding: String): Gen[String] =
    Gen.oneOf(
      Gen.const(""),
      Gen.const(s"<?xml version='$version' encoding='$encoding'?>")
    )

  val genXmlString: Gen[String] = for {
    document <- Arbitrary.arbitrary[Document]
    encoding <- Gen.const("UTF-8") // java.nio.charset.StandardCharsets.UTF_8.name
    xmlDecl <- xmlDeclGen("1.0", encoding)
  } yield {
    val str = xmlDecl + Group(document.children ++ Seq(document.docElem)).toString
    str
  }
}
