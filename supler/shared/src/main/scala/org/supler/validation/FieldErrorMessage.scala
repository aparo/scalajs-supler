/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.validation

import org.supler.{ FieldPath, Message }
import org.supler.field.Field
import play.api.libs.json._

case class FieldErrorMessage(field: Field[_], path: FieldPath, message: Message) {
  def generateJSON = {
    Json.obj(
      "field_path" -> JsString(path.toString),
      "error_key" -> JsString(message.key),
      "error_params" -> JsArray(message.params.map(p => JsString(p.toString)).toList))
  }
}
