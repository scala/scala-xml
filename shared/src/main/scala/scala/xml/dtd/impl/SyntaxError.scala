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
package xml.dtd.impl

/** This runtime exception is thrown if an attempt to instantiate a
  *  syntactically incorrect expression is detected.
  *
  *  @author  Burak Emir
  */
@deprecated("This class will be removed", "2.10.0")
private[dtd] class SyntaxError(e: String) extends RuntimeException(e)
