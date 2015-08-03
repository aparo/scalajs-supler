/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.field

import org.supler._
import play.api.libs.json
import play.api.libs.json._

trait GenerateBasicJSON[T] {
  this: Field[T] =>

  def renderHint: Option[RenderHint]

  private[supler] override def generateFieldJSON(parentPath: FieldPath, obj: T) = {
    val data = generateJSONData(obj)

    import JSONFieldNames._

    Json.obj(
      Type -> JsString(data.fieldTypeName),
      Validate -> JsObject(data.validationJSON),
      Path -> JsString(parentPath.append(name).toString)
    )  ++ JsObject( (data.valueJSONValue.map(v => Value-> v)
      ++ data.emptyValue.map(v => EmptyValue -> v)
      ++ generateRenderHintJSONValue.map(v => RenderHint -> v)
      ++ data.extraJSON).toSeq)
  }

  protected def generateJSONData(obj: T): BasicJSONData

  private def generateRenderHintJSONValue= renderHint.map(rh => JsObject(
    Seq("name" -> JsString(rh.name))) ++ JsObject(rh.extraJSON))

  case class BasicJSONData(
    fieldTypeName: String,
    valueJSONValue: Option[JsValue],
    validationJSON: List[(String, JsValue)],
    emptyValue: Option[JsValue],
    extraJSON: List[(String, JsValue)] = Nil
  )
}
