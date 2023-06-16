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

/**
 * an ExternalIDs - either PublicID or SystemID
 *
 *  @author Burak Emir
 */
sealed abstract class ExternalID extends parsing.TokenTests {
  def quoted(s: String): String = {
    val c: Char = if (s.contains('"')) '\'' else '"'
    s"$c$s$c"
  }

  // public != null: PUBLIC " " publicLiteral " " [systemLiteral]
  // public == null: SYSTEM " " systemLiteral
  override def toString: String =
    if (publicId == null) s"SYSTEM ${quoted(systemId)}" else
    if (systemId == null) s"PUBLIC ${quoted(publicId)}" else
      s"PUBLIC ${quoted(publicId)} ${quoted(systemId)}"

  def buildString(sb: StringBuilder): StringBuilder =
    sb.append(this.toString)

  def systemId: String
  def publicId: String
}

/**
 * a system identifier
 *
 *  @author Burak Emir
 *  @param  systemId the system identifier literal
 */
case class SystemID(override val systemId: String) extends ExternalID {
  override val publicId: scala.Null = null

  if (!checkSysID(systemId))
    throw new IllegalArgumentException("can't use both \" and ' in systemId")
}

/**
 * a public identifier (see http://www.w3.org/QA/2002/04/valid-dtd-list.html).
 *
 *  @author Burak Emir
 *  @param  publicId the public identifier literal
 *  @param  systemId (can be null for notation pubIDs) the system identifier literal
 */
case class PublicID(override val publicId: String, override val systemId: String) extends ExternalID {
  if (!checkPubID(publicId))
    throw new IllegalArgumentException("publicId must consist of PubidChars")

  if (systemId != null && !checkSysID(systemId))
    throw new IllegalArgumentException("can't use both \" and ' in systemId")

  /** the constant "#PI" */
  def label: String = "#PI"

  /** always empty */
  def attribute: ScalaVersionSpecificReturnTypes.ExternalIDAttribute = Node.NoAttributes

  /** always empty */
  def child: Nil.type = Nil
}

/**
 * A marker used when a `DocType` contains no external id.
 *
 *  @author Michael Bayne
 */
object NoExternalID extends ExternalID {
  override val publicId: ScalaVersionSpecificReturnTypes.NoExternalIDId = null
  override val systemId: ScalaVersionSpecificReturnTypes.NoExternalIDId = null

  override def toString: String = ""
}
