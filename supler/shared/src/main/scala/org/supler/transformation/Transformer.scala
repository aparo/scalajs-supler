/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.transformation

import org.supler.field.{BasicFieldCompatible, RenderHint}
import play.api.libs.json.JsValue

class Transformer[U, S](basicTypeTransformer: BasicTypeTransformer[U, S], jsonTransformer: JsonTransformer[S]) {
  def serialize(u: U): Option[JsValue] = jsonTransformer.toJValueOrJNull(basicTypeTransformer.serialize(u))

  def deserialize(jvalue: JsValue): Either[String, U] = for {
    s <- jsonTransformer.fromJValue(jvalue).toRight("cannot convert json value").right
    u <- basicTypeTransformer.deserialize(s).right
  } yield u

  def typeName = jsonTransformer.typeName

  def renderHint: Option[RenderHint with BasicFieldCompatible] = basicTypeTransformer.renderHint
  }

object Transformer {
  implicit def createFromBasicAndJson[U, S](
    implicit basicTypeTransformer: BasicTypeTransformer[U, S], jsonTransformer: JsonTransformer[S]): Transformer[U, S] =
    new Transformer[U, S](basicTypeTransformer, jsonTransformer)

  implicit def createFromJson[S](
    implicit jsonTransformer: JsonTransformer[S]): Transformer[S, S] =
    new Transformer[S, S](new BasicTypeTransformer.IdentityTransformer[S] {}, jsonTransformer)
}
