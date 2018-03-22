package scala.xml

import scala.xml.transform._
import scala.collection.Seq
import org.junit.Assert.assertSame
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.experimental.theories.DataPoints
import org.junit.runner.RunWith
/**
 * This test verifies that after the transform, the resultant xml node
 * uses as many old nodes as possible. 
 * 
 * Three transformers class for case - 
 * One for original, one for modified, and one proposed which shows 
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

  @DataPoints 
  def tranformers() = Array(
      new OriginalTranformr(rewriteRule),
      new ModifiedTranformr(rewriteRule),
      new AlternateTranformr(rewriteRule))
}

@RunWith(classOf[Theories])
class ReuseNodesTest {
   
  @Theory
  def transformReferentialEquality(rt:RuleTransformer) = { 
    val original = <p><lost/></p>
    val tranformed = rt.transform(original)
    assertSame(original, tranformed)
  }
      
  @Theory
  def transformReferentialEqualityOnly(rt:RuleTransformer) = { 
    val original = <changed><change><lost/><a><b><c/></b></a></change><a><b><c/></b></a></changed>
    val transformed = rt.transform(original)
    recursiveAssert(original,transformed)
  }
  
  def recursiveAssert(original:Seq[Node], transformed:Seq[Node]):Unit = {
    original zip transformed foreach { 
      case (x, y) => recursiveAssert(x, y) 
    }
  }
  
  def recursiveAssert(original:Node, transformed:Node):Unit = {
    transformed.label match { 
      case "changed" => // do nothing expect this node to be changed
        recursiveAssert(original.child,transformed.child)
      case _ => {
        assertSame(original, transformed)
        // No need to check for children, node being immuatable 
        // children can't be different if parents are referentially equal
      }
    }
  }
}
