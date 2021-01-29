/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2020, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    (c) 2011-2020, Lightbend, Inc.       **
** /____/\___/_/ |_/____/_/ | |    http://scala-lang.org/               **
**                          |/                                          **
\*                                                                      */

package scala
package xml
package include

/** An `UnavailableResourceException` is thrown when an included document
  * cannot be found or loaded.
  */
class UnavailableResourceException(message: String)
    extends XIncludeException(message) {
  def this() = this(null)
}
