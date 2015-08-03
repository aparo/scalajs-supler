/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler

import scala.reflect.macros.blackbox

object SuplerFormMacros {
  def form_impl[T: c.WeakTypeTag](c: blackbox.Context)(rows: c.Expr[Supler[T] => List[Row[T]]]): c.Expr[Form[T]] = {

    import c.universe._

    val targetTpe = implicitly[WeakTypeTag[T]].tpe
    val constructorOpt = targetTpe.members.find(m => m.isMethod && m.asMethod.isPrimaryConstructor)
    val empty = constructorOpt match {
      case None =>
        c.abort(c.enclosingPosition, "Cannot find constructor for " + targetTpe)
      case Some(targetConstructor) =>
        val targetConstructorParamLists = targetConstructor.asMethod.paramLists
        val TypeRef(_, sym, tpeArgs) = targetTpe
        var newT: Tree = Select(New(Ident(targetTpe.typeSymbol)), termNames.CONSTRUCTOR)

        for {
          targetConstructorParams <- targetConstructorParamLists
        } {
          val constructorParams: List[c.Tree] = for (param <- targetConstructorParams) yield {
            val pTpe = param.typeSignature.substituteTypes(sym.asClass.typeParams, tpeArgs)
            SuplerFieldMacros.defaultForType(c)(pTpe).getOrElse(reify { null }).tree
          }

          newT = Apply(newT, constructorParams)
        }

        c.Expr(newT)
    }

    reify {
      Form(rows.splice(new Supler[T] {}), () => empty.splice)
    }
  }
}
