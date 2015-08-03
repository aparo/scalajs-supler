/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.field


import org.supler.validation.PartiallyAppliedObj
import org.supler.{FieldPath, MultiFieldRow, Row, RowsJSON}
import play.api.libs.json._

trait Field[T] extends Row[T] {
  def name: String
  def label: Option[String]
  def description: Option[String]

  private[supler] def enabledIf: T => Boolean
  private[supler] def includeIf: T => Boolean

  override def ||(field: Field[T]): Row[T] = MultiFieldRow(this :: field :: Nil)

  private[supler] override def generateJSON(parentPath: FieldPath, obj: T): RowsJSON = {
    val isIncluded = includeIf(obj)
    if (isIncluded) {
      val isEnabled = enabledIf(obj)

      val fieldJsonPartial = generateFieldJSON(parentPath, obj)
      val commonJson = List(JSONFieldNames.Name -> JsString(name),
        JSONFieldNames.Enabled -> JsBoolean(isEnabled),
        JSONFieldNames.Label -> JsString(label.getOrElse(""))) ++
        description.map(d => JSONFieldNames.Description -> JsString(d))
      val fieldJson = JsObject(commonJson)++ fieldJsonPartial

      RowsJSON.singleField(fieldJson, name)
    } else RowsJSON.empty
  }

  override private[supler] def applyJSONValues(parentPath: FieldPath, obj: T, jsonFields: Map[String, JsValue]) = {
    if (includeIf(obj) && enabledIf(obj)) {
      applyFieldJSONValues(parentPath, obj, jsonFields)
    } else PartiallyAppliedObj.full(obj)
  }

  private[supler] def applyFieldJSONValues(parentPath: FieldPath, obj: T, jsonFields: Map[String, JsValue]): PartiallyAppliedObj[T]
  private[supler] def generateFieldJSON(parentPath: FieldPath, obj: T): JsObject

  protected object JSONFieldNames {
    val Name = "name"
    val Type = "type"
    val Label = "label"
    val Description = "description"
    val Multiple = "multiple"
    val Value = "value"
    val Validate = "validate"
    val RenderHint = "render_hint"
    val PossibleValues = "possible_values"
    val Path = "path"
    val EmptyValue = "empty_value"
    val Icon = "icon"
    val Enabled = "enabled"

    val ValidateRequired = "required"
  }

  protected object SpecialFieldTypes {
    val Select = "select"
    val MultiEdit = "multiedit"
    val Subform = "subform"
    val Static = "static"
    val Action = "action"
  }

  private[supler] override def findAction(
    parentPath: FieldPath,
    obj: T,
    jsonFields: Map[String, JsValue],
    ctx: RunActionContext): Option[RunnableAction] = None
}
