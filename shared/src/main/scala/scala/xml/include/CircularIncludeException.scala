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

import xml.Nullables._

/**
 *  A `CircularIncludeException` is thrown when an included document attempts
 *  to include itself or one of its ancestor documents.
 */
class CircularIncludeException(message: Nullable[String]) extends XIncludeException {

  /**
   * Constructs a `CircularIncludeException` with `'''null'''`.
   * as its error detail message.
   */
  def this() = this(null)
}
