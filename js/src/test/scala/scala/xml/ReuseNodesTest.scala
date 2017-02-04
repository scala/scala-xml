package scala.xml

import scala.xml.transform._
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
/**
 * This test verify that after the tranform, the resultant xml node
 * uses as many old nodes as possible.
 *
 * Three transformers class for case -
 * One for orginal, one for modified, and one proposed which shows
 * all are equivalent when it comes to reusing as many nodes as possible
 */
object ReuseNodesTest {

  class OriginalTranformr(rules: RewriteRule*) extends RuleTransformer(rules:_*) {
    override def transform(ns: Seq[Node]): Seq[Node] = {
      val xs = ns.toStream map transform
      val (xs1, xs2) = xs zip ns span { case (x, n) => unchanged(n, x) }

      if (xs2.isEmpty) ns
      else (xs1 map (_._2)) ++ xs2.head._1 ++ transform(ns drop (xs1.length + 1))
    }
    override def transform(n:Node): Seq[Node] = super.transform(n)
  }

  class ModifiedTranformr(rules: RewriteRule*) extends RuleTransformer(rules:_*) {
    override def transform(ns: Seq[Node]): Seq[Node] = {
      val changed = ns flatMap transform

      if (changed.length != ns.length || (changed, ns).zipped.exists(_ != _)) changed
      else ns
    }
    override def transform(n:Node): Seq[Node] = super.transform(n)
  }

  class AlternateTranformr(rules: RewriteRule*) extends RuleTransformer(rules:_*) {
    override def transform(ns: Seq[Node]): Seq[Node] = {
      val xs = ns.toStream map transform
      val (xs1, xs2) = xs zip ns span { case (x, n) => unchanged(n, x) }

      if (xs2.isEmpty) ns
      else (xs1 map (_._2)) ++ xs2.head._1 ++ transform(ns drop (xs1.length + 1))
    }
    override def transform(n:Node): Seq[Node] = super.transform(n)
  }

  def rewriteRule = new RewriteRule {
    override def transform(n: Node): NodeSeq = n match {
      case n if n.label == "change" => Elem(
           n.prefix, "changed", n.attributes, n.scope, n.child.isEmpty, n.child : _*)
      case _ => n
    }
  }

}
