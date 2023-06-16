/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

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
  var externalID: ExternalID = _
  var decls: List[Decl] = Nil
  def notations: Seq[NotationDecl] = Nil
  def unparsedEntities: Seq[EntityDecl] = Nil

  var elem: mutable.Map[String, ElemDecl] = new mutable.HashMap[String, ElemDecl]()
  var attr: mutable.Map[String, AttListDecl] = new mutable.HashMap[String, AttListDecl]()
  var ent: mutable.Map[String, EntityDecl] = new mutable.HashMap[String, EntityDecl]()

  override def toString: String =
    s"DTD ${Option(externalID).getOrElse("")} [\n${decls.mkString("\n")}\n]"
}
