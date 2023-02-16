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

class Tokens {

  // Tokens

  final val TOKEN_PCDATA: Int = 0
  final val NAME: Int = 1
  final val LPAREN: Int = 3
  final val RPAREN: Int = 4
  final val COMMA: Int = 5
  final val STAR: Int = 6
  final val PLUS: Int = 7
  final val OPT: Int = 8
  final val CHOICE: Int = 9
  final val END: Int = 10
  final val S: Int = 13

  final def token2string(i: Int): String = i match {
    case 0  => "#PCDATA"
    case 1  => "NAME"
    case 3  => "("
    case 4  => ")"
    case 5  => ","
    case 6  => "*"
    case 7  => "+"
    case 8  => "?"
    case 9  => "|"
    case 10 => "END"
    case 13 => " "
  }
}
