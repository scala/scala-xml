/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2020, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    (c) 2011-2020, Lightbend, Inc.       **
** /____/\___/_/ |_/____/_/ | |    http://scala-lang.org/               **
**                          |/                                          **
\*                                                                      */

package scala
package xml
package dtd

import scala.collection.mutable
import scala.collection.Seq

/**
 * A document type declaration.
 *
 *  @author Burak Emir
 */
abstract class DTD {
  var externalID: ExternalID = null
  var decls: List[Decl] = Nil
  def notations: Seq[NotationDecl] = Nil
  def unparsedEntities: Seq[EntityDecl] = Nil

  var elem: mutable.Map[String, ElemDecl] = new mutable.HashMap[String, ElemDecl]()
  var attr: mutable.Map[String, AttListDecl] = new mutable.HashMap[String, AttListDecl]()
  var ent: mutable.Map[String, EntityDecl] = new mutable.HashMap[String, EntityDecl]()

  override def toString() =
    "DTD [\n%s%s]".format(
      Option(externalID) getOrElse "",
      decls.mkString("", "\n", "\n")
    )
}
