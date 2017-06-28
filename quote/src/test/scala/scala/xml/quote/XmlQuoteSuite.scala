package scala.xml.quote

import org.scalatest.{FunSuite, Matchers}

abstract class XmlQuoteSuite extends FunSuite with Matchers {

  implicit class NodeOps(val self: xml.Node) {
    /** Like `==` with scope comparison */
    def ≈(that: xml.Node): Boolean =
      self == that && hasSameScope(self, that)

    def !≈(that: xml.Node): Boolean = !(self ≈ that)

    private def hasSameScope(self: xml.Node, that: xml.Node): Boolean =
      self.scope == that.scope && {
        val zipped = (self, that) match {
          case (g1: xml.Group, g2: xml.Group) => (g1.nodes, g2.nodes).zipped
          case (n1, n2)                       => (n1.child, n2.child).zipped
        }
        zipped.forall(hasSameScope)
      }
  }

  implicit class NodeBufferOps(val self: xml.NodeBuffer) {
    /** Like `==` with scope comparison */
    def ≈(that: xml.NodeBuffer): Boolean = {
      val selfIt = self.iterator
      val thatIt = that.iterator

      while (selfIt.hasNext && thatIt.hasNext) {
        if (!(selfIt.next() ≈ thatIt.next())) {
          return false
        }
      }

      selfIt.isEmpty && thatIt.isEmpty
    }

    def !≈(that: xml.NodeBuffer): Boolean = !(self ≈ that)
  }
}
