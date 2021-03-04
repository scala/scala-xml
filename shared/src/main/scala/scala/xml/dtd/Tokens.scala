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

  final val TOKEN_PCDATA = 0
  final val NAME = 1
  final val LPAREN = 3
  final val RPAREN = 4
  final val COMMA = 5
  final val STAR = 6
  final val PLUS = 7
  final val OPT = 8
  final val CHOICE = 9
  final val END = 10
  final val S = 13

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
