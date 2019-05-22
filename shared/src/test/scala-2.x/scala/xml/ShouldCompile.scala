package scala.xml

// these tests depend on xml, so they ended up here,
// though really they are compiler tests

import scala.collection._
import scala.collection.mutable.ArrayBuffer

// t1626
object o {
  val n = <a xmlns=""/>
  n.namespace == null
}
