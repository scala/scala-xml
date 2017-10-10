/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

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
