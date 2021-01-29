/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2020, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    (c) 2011-2020, Lightbend, Inc.       **
** /____/\___/_/ |_/____/_/ | |    http://scala-lang.org/               **
**                          |/                                          **
\*                                                                      */

package scala
package xml.dtd.impl

/** This runtime exception is thrown if an attempt to instantiate a
  *  syntactically incorrect expression is detected.
  *
  *  @author  Burak Emir
  */
@deprecated("This class will be removed", "2.10.0")
private[dtd] class SyntaxError(e: String) extends RuntimeException(e)
