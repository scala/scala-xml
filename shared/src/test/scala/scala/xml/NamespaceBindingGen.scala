/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

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
