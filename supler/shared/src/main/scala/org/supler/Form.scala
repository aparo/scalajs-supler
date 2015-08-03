/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler

import java.util.UUID

import org.supler.field._
import org.supler.validation._
import play.api.libs.json._

case class Form[T](rows: List[Row[T]], createEmpty: () => T) {
  requireFieldsUnique()

  var id: String = UUID.randomUUID().toString.replace("-", "")

  var options: JsValue = JsObject(Nil)

  def apply(obj: T): FormWithObject[T] = InitialFormWithObject(this, obj, None, FormMeta(Map()))

  def withNewEmpty: FormWithObject[T] = InitialFormWithObject(this, createEmpty(), None, FormMeta(Map()))

  def getMeta(jvalue: JsValue): FormMeta = FormMeta.fromJSON(jvalue)

  private[supler] def doValidate(parentPath: FieldPath, obj: T, scope: ValidationScope): FieldErrors =
    rows.flatMap(_.doValidate(parentPath, obj, scope))

  private[supler] def generateJSON(parentPath: FieldPath, obj: T): JsValue = {
    val rowsJSONs = rows.map(_.generateJSON(parentPath, obj))
    JsObject(Seq(
      "fields" -> JsArray(rowsJSONs.flatMap(_.fields)),
      "fieldOrder" -> JsArray(rowsJSONs.map(_.fieldOrderAsJSON)),
      "id" -> JsString(id),
      "options" -> options))
  }

  private[supler] def applyJSONValues(parentPath: FieldPath, obj: T, jvalue: JsValue): PartiallyAppliedObj[T] = {
    jvalue match {
      case JsObject(jsonFields) => Row.applyJSONValues(rows, parentPath, obj, jsonFields.toMap)
      case _ => PartiallyAppliedObj.full(obj)
    }
  }

  /**
   * Finds the action specified in the given json (`jvalue`), if any. The action finding and action running has to be
   * separated, so that after the action is found, validation of the correct scope can be run (e.g. the whole form),
   * and only then the action can be executed.
   */
  private[supler] def findAction(parentPath: FieldPath, obj: T, jvalue: JsValue, ctx: RunActionContext): Option[RunnableAction] = {
    jvalue match {
      case JsObject(jsonFields) => Row.findFirstAction(parentPath, rows, obj, jsonFields.toMap, ctx)
      case _ => None
    }
  }

  private def requireFieldsUnique() {
    val fieldsUsedMultipletimes = rows.flatMap {
      case MultiFieldRow(fields) => fields
      case f: Field[_] => List(f)
      case _ => Nil
    }.groupBy(f => f.name).filter(_._2.size > 1).map(_._1)

    require(fieldsUsedMultipletimes.isEmpty,
      "Supler does not support same field multiple times on a form, but those were used: "+fieldsUsedMultipletimes.mkString(", "))
  }

  def useCreateEmpty(newCreateEmpty: => T) = this.copy(createEmpty = () => newCreateEmpty)

  def +(row: Row[T]) = ++(List(row))

  def ++(moreRows: List[Row[T]]) = Form(rows ++ moreRows, createEmpty)
}
