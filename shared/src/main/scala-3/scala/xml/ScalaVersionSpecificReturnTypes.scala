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

/*
 Unlike other Scala-version-specific things, this class is not filling any gaps in capabilities
 between different versions of Scala; instead, it mostly documents the types that different versions of the
 Scala compiler inferred in the unfortunate absence of the explicit type annotations.
 What should have been specified explicitly is given in the comments;
 next time we break binary compatibility the types should be changed in the code and this class removed.
 */
private[xml] object ScalaVersionSpecificReturnTypes { // should be
  type ExternalIDAttribute = MetaData                 // Null.type
  type NoExternalIDId = String                        // scala.Null
  type NodeNoAttributes = MetaData                    // Null.type
  type NullFilter = MetaData                          // Null.type
  type NullGetNamespace = String                      // scala.Null
  type NullNext = MetaData                            // scala.Null
  type NullKey = String                               // scala.Null
  type NullValue = scala.collection.Seq[Node]         // scala.Null
  type NullApply1 = scala.collection.Seq[Node]        // scala.Null
  type NullApply3 = scala.collection.Seq[Node]        // scala.Null
  type NullRemove = MetaData                          // Null.type
  type SpecialNodeChild = scala.collection.Seq[Node]  // Nil.type
  type GroupChild = scala.collection.Seq[Node]        // Nothing
}
