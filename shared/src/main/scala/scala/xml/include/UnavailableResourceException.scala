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
package include

/** An `UnavailableResourceException` is thrown when an included document
  * cannot be found or loaded.
  */
class UnavailableResourceException(message: String)
    extends XIncludeException(message) {
  def this() = this(null)
}
