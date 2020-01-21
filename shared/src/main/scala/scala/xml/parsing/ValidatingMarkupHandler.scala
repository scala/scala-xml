/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2019, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package xml
package parsing

import scala.xml.dtd._

@deprecated("This class will be removed", "1.3.0")
abstract class ValidatingMarkupHandler extends MarkupHandler {

  var rootLabel: String = _
  var qStack: List[Int] = Nil
  var qCurrent: Int = -1

  var declStack: List[ElemDecl] = Nil
  var declCurrent: ElemDecl = null

  final override val isValidating = true

  override def endDTD(n: String) = {
    rootLabel = n
  }
  override def elemStart(pos: Int, pre: String, label: String, attrs: MetaData, scope: NamespaceBinding): Unit = {

    def advanceDFA(dm: DFAContentModel) = {
      val trans = dm.dfa.delta(qCurrent)
      // println("advanceDFA(dm): " + dm)
      // println("advanceDFA(trans): " + trans)
      trans.get(ContentModel.ElemName(label)) match {
        case Some(qNew) => qCurrent = qNew
        case _          => reportValidationError(pos, "DTD says, wrong element, expected one of " + trans.keys)
      }
    }
    // advance in current automaton
    // println("[qCurrent = " + qCurrent + " visiting " + label + "]")

    if (qCurrent == -1) { // root
      // println("  checking root")
      if (label != rootLabel)
        reportValidationError(pos, "this element should be " + rootLabel)
    } else {
      // println("  checking node")
      declCurrent.contentModel match {
        case ANY =>
        case EMPTY =>
          reportValidationError(pos, "DTD says, no elems, no text allowed here")
        case PCDATA =>
          reportValidationError(pos, "DTD says, no elements allowed here")
        case m@MIXED(r) =>
          advanceDFA(m)
        case e@ELEMENTS(r) =>
          advanceDFA(e)
      }
    }
    // push state, decl
    qStack = qCurrent :: qStack
    declStack = declCurrent :: declStack

    declCurrent = lookupElemDecl(label)
    qCurrent = 0
    // println("  done  now")
  }

  override def elemEnd(pos: Int, pre: String, label: String): Unit = {
    // println("  elemEnd")
    qCurrent = qStack.head
    qStack = qStack.tail
    declCurrent = declStack.head
    declStack = declStack.tail
    // println("    qCurrent now" + qCurrent)
    // println("    declCurrent now" + declCurrent)
  }

  final override def elemDecl(name: String, cmstr: String): Unit = {
    decls = ElemDecl(name, ContentModel.parse(cmstr)) :: decls
  }

  final override def attListDecl(name: String, attList: List[AttrDecl]): Unit = {
    decls = AttListDecl(name, attList) :: decls
  }

  final override def unparsedEntityDecl(name: String, extID: ExternalID, notat: String): Unit = {
    decls = UnparsedEntityDecl(name, extID, notat) :: decls
  }

  final override def notationDecl(notat: String, extID: ExternalID): Unit = {
    decls = NotationDecl(notat, extID) :: decls
  }

  final override def peReference(name: String): Unit = {
    decls = PEReference(name) :: decls
  }

  /** report a syntax error */
  def reportValidationError(pos: Int, str: String): Unit
}
