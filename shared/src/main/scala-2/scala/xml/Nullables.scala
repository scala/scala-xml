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

package scala.xml
object Nullables {
    type Nullable[T] = T
    implicit class NonNullOps[T](private val x: T) extends AnyVal {
        def nn: T = {
            x
        }
    }
}
