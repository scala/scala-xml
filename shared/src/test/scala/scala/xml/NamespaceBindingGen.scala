package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait NamespaceBindingGen {

  val genDepth: Gen[NamespaceBinding] = for {
    prefix <- Arbitrary.arbitrary[String] if !prefix.isEmpty
    uri <- Arbitrary.arbitrary[String]
    parent <- genNamespaceBinding
  } yield {
    NamespaceBinding(prefix, uri, parent)
  }

  val genNamespaceBinding: Gen[NamespaceBinding] = for {
    // FIXME: Throws SAXParseException: prefix "{0}" was not declared.
    // Need to writer generator that can write namespace declaration
    // namespace <- Gen.oneOf(Gen.const(TopScope), genDepth)
    namespace <- Gen.const(TopScope)
  } yield {
    namespace
  }

  implicit val arbNamespaceBinding = Arbitrary {
    genNamespaceBinding
  }
}
