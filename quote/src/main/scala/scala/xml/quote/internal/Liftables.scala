package scala.xml.quote.internal

import scala.xml.quote.internal.ast._

/** Lift `ast.Node` to `c.universe.Tree`.
  *
  * At this point, `Node` are expected to be valid.
  *
  * Note: `$_scope` is used as a scope name because `$scope` is already taken.
  */
private[internal] trait Liftables { self: QuoteImpl =>
  import Liftables.{Scope, TopScope}
  import self.c.universe._

  def lift(nodes: Seq[Node]): Tree = {
    val tree =
      if (nodes.size == 1) liftNode(TopScope)(nodes.head)
      else liftNodes(TopScope)(nodes)
    fixScopes(tree)
  }


  /** When we lift, we don't know if we are within an enclosing xml element
    * which defines a scope. In some cases we will have to fix the scope.
    *
    * E.g:
    * {{{
    *   xml"""<a xmlns:pre="scope0">${ xml"<b/>" }</a>"""
    * }}}
    * Here the scope of `<b/>` is `TopScope` but should be `scope0`
    */
  private def fixScopes(tree: Tree): Tree = {
    val typed = c.typecheck(tree)

    var scopeSym = NoSymbol
    c.internal.typingTransform(typed)((tree, api) => tree match {
      case q"$_.TopScope" if scopeSym != NoSymbol =>
        api.typecheck(q"$scopeSym")
      case q"val $$_scope = $_" => // this assignment is only here when creating new scope
        scopeSym = tree.symbol
        tree
      case _ =>
        api.default(tree)
    })
  }

  private val sx = q"_root_.scala.xml"

  private implicit def liftNode(implicit outer: Scope): Liftable[Node] =
    Liftable {
      case n: Group       => liftGroup(outer)(n)
      case n: Elem        => liftElem(outer)(n)
      case n: Text        => liftText(n)
      case n: Placeholder => liftPlaceholder(n)
      case n: Comment     => liftComment(n)
      case n: PCData      => liftPCData(n)
      case n: ProcInstr   => liftProcInstr(n)
      case n: Unparsed    => liftUnparsed(n)
      case n: EntityRef   => liftEntityRef(n)
    }

  private implicit def liftNodes(implicit outer: Scope): Liftable[Seq[Node]] = Liftable { nodes =>
    val additions = nodes.map(node => q"$$buf &+ $node")
    q"""
      {
        val $$buf = new $sx.NodeBuffer
        ..$additions
        $$buf
      }
    """
  }

  private def liftGroup(implicit outer: Scope) = Liftable { gr: Group =>
    q"new $sx.Group(${gr.nodes})"
  }

  private def liftElem(implicit outer: Scope) = Liftable { e: Elem =>
    def outerScope =
      if (outer.isTopScope) q"$sx.TopScope"
      else q"$$_scope"

    def liftAttributes(atts: Seq[Attribute]): Seq[Tree] = {
      val metas = atts.reverse.map { a =>
        val value = a.value match {
          case Seq(v) => q"$v"
          case vs     => q"$vs"
        }

        val att =
          if (a.prefix.isEmpty) q"new $sx.UnprefixedAttribute(${a.key}, $value, $$md)"
          else q"new $sx.PrefixedAttribute(${a.prefix}, ${a.key}, $value, $$md)"

        q"$$md = $att"
      }

      val init: Tree = q"var $$md: $sx.MetaData = $sx.Null"
      init +: metas
    }

    def liftNameSpaces(nss: Seq[Attribute]): Seq[Tree] = {
      val init: Tree = q"var $$tmpscope: $sx.NamespaceBinding = $outerScope"

      val scopes = nss.map { ns =>
        val prefix = if (ns.prefix.nonEmpty) q"${ns.key}" else q"null: String"
        val uri = ns.value.head match {
          case Text(text, _) => q"$text"
          case scalaExpr     => q"$scalaExpr"
        }
        q"$$tmpscope = new $sx.NamespaceBinding($prefix, $uri, $$tmpscope)"
      }

      init +: scopes
    }

    val (nss, atts) = e.attributes.partition(_.isNamespace)

    val prefix: Tree =
      if (e.prefix.isEmpty) q"null: String"
      else q"${e.prefix}"

    val label = q"${e.label}"

    val (metapre, metaval) =
      if (atts.isEmpty) (Nil, q"$sx.Null")
      else (liftAttributes(atts), q"$$md")

    val minimizeEmpty = q"${e.minimizeEmpty}"

    def children = {
      val newScope = new Scope(outer.isTopScope && nss.isEmpty)
      liftNodes(newScope)(e.children)
    }

    def newElem(scope: Tree) =
      if (e.children.isEmpty) q"new $sx.Elem($prefix, $label, $metaval, $scope, $minimizeEmpty)"
      else q"new $sx.Elem($prefix, $label, $metaval, $scope, $minimizeEmpty, $children: _*)"

    if (nss.isEmpty) {
      q"""
        {
          ..$metapre
          ${newElem(outerScope)}
        }
       """
    } else {
      val scopepre = liftNameSpaces(nss)
      q"""
        {
          ..$scopepre;
          {
            val $$_scope = $$tmpscope
            ..$metapre
            ${newElem(q"$$_scope")}
          }
        }
       """
    }
  }

  private val liftText = Liftable { t: Text =>
    q"new $sx.Text(${t.text})"
  }

  private val liftPlaceholder = Liftable { p: Placeholder =>
    self.arg(p.id)
  }

  private val liftComment = Liftable { c: Comment =>
    q"new $sx.Comment(${c.text})"
  }

  private val liftPCData = Liftable { pcd: PCData =>
    q"new $sx.PCData(${pcd.data})"
  }

  private val liftProcInstr = Liftable { pi: ProcInstr =>
    q"new $sx.ProcInstr(${pi.target}, ${pi.proctext})"
  }

  private val liftUnparsed = Liftable { u: Unparsed =>
    q"new $sx.Unparsed(${u.data})"
  }

  private val liftEntityRef = Liftable { er: EntityRef =>
    q"new $sx.EntityRef(${er.name})"
  }
}

private object Liftables {
  class Scope(val isTopScope: Boolean) extends AnyVal
  final val TopScope = new Scope(true)
}
