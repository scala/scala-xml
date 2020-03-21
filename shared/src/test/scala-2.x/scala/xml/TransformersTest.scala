package scala.xml

import scala.collection.Seq
import scala.xml.transform._

import org.junit.Test
import org.junit.Assert.assertEquals

class TransformersTest {


  def transformer = new RuleTransformer(new RewriteRule {
    override def transform(n: Node): NodeSeq = n match {
      case <t>{ _* }</t> => <q/>
      case n => n
    }
  })

  @Test
  def transform = // SI-2124
    assertEquals(transformer.transform(<p><lost/><t><s><r></r></s></t></p>),
      <p><lost/><q/></p>)

  @Test
  def transformNamespaced = // SI-2125
    assertEquals(transformer.transform(<xml:group><p><lost/><t><s><r></r></s></t></p></xml:group>),
      Group(<p><lost/><q/></p>))

  @Test
  def rewriteRule = { // SI-2276
    val inputXml: Node =
      <root>
        <subnode>
          <version>1</version>
        </subnode>
        <contents>
          <version>1</version>
        </contents>
      </root>

    object t1 extends RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case <version>{ x }</version> if x.toString.toInt < 4 => <version>{ x.toString.toInt + 1 }</version>
        case other => other
      }
    }

    val ruleTransformer = new RuleTransformer(t1)
    JUnitAssertsForXML.assertEquals(ruleTransformer(inputXml).toString, // TODO: why do we need toString?
      <root>
        <subnode>
          <version>2</version>
        </subnode>
        <contents>
          <version>2</version>
        </contents>
      </root>)
  }

  @Test
  def preserveReferentialComplexityInLinearComplexity = { // SI-4528
    var i = 0

    val xmlNode = <a><b><c><h1>Hello Example</h1></c></b></a>

    new RuleTransformer(new RewriteRule {
      override def transform(n: Node): Seq[Node] = {
        n match {
          case t: Text if !t.text.trim.isEmpty => {
            i += 1
            Text(t.text + "!")
          }
          case _ => n
        }
      }
    }).transform(xmlNode)

    assertEquals(1, i)
  }

  @Test
  def appliesRulesRecursivelyOnPreviousChanges = { // #257
    def add(outer: Elem, inner: Node) = new RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case e: Elem if e.label == outer.label => e.copy(child = e.child ++ inner)
        case other => other
      }
    }

    def transformer = new RuleTransformer(add(<element/>, <new/>), add(<new/>, <thing/>))

    assertEquals(<element><new><thing/></new></element>, transformer(<element/>))
  }
}

