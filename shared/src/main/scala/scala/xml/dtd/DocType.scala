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

import scala.collection.Seq

/**
 * An XML node for document type declaration.
 *
 *  @author Burak Emir
 *
 *  @param  name   name of this DOCTYPE
 *  @param  extID  NoExternalID or the external ID of this doctype
 *  @param  intSubset sequence of internal subset declarations
 */
case class DocType(name: String, extID: ExternalID, intSubset: Seq[dtd.Decl]) {
  if (!Utility.isName(name))
    throw new IllegalArgumentException(s"$name must be an XML Name")

  /** returns "&lt;!DOCTYPE + name + extID? + ("["+intSubSet+"]")? >" */
  final override def toString: String = {
    def intString: String =
      if (intSubset.isEmpty) ""
      else intSubset.mkString("[", "", "]")

    s"<!DOCTYPE $name $extID$intString>"
  }
}

object DocType {
  /** Creates a doctype with no external id, nor internal subset declarations. */
  def apply(name: String): DocType = apply(name, NoExternalID, Nil)
}
