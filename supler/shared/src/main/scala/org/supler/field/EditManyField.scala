/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.field

import org.supler._
import org.supler.validation._
import play.api.libs.json._

case class EditManyField[T, U](
    name: String,
    read: T => List[U],
    write: (T, List[U]) => T,
    validators: List[Validator[T, List[U]]],
    label: Option[String],
    description: Option[String],
    labelForValue: U => String,
    valuesProvider: ValuesProvider[T, U],
    idForValue: Option[U => String],
    renderHint: Option[RenderHint with EditManyFieldCompatible],
    enabledIf: T => Boolean,
    includeIf: T => Boolean) extends Field[T] with SelectField[T, U] with ValidateWithValidators[T, List[U]] {

  override def fieldType = SpecialFieldTypes.MultiEdit

  def label(newLabel: String): EditManyField[T, U] = this.copy(label = Some(newLabel))
  def validate(validators: Validator[T, List[U]]*): EditManyField[T, U] = this.copy(validators = this.validators ++ validators)
  def renderHint(newRenderHint: RenderHint with EditManyFieldCompatible): EditManyField[T, U] = this.copy(renderHint = Some(newRenderHint))

  def enabledIf(condition: T => Boolean): EditManyField[T, U] = this.copy(enabledIf = condition)
  def includeIf(condition: T => Boolean): EditManyField[T, U] = this.copy(includeIf = condition)

  def idForValue[I](idFn: U => I)(implicit idTransformer: SelectValueIdSerializer[I]): EditManyField[T, U] =
    this.copy(idForValue = Some(idFn andThen idTransformer.toString))

  override def emptyValue = None
  override def required = false

  protected def multiple = true

  protected def generateValueJSONData(obj: T) = {
    val currentValues = read(obj)

    ValueJSONData(Some(JsArray(currentValues.toList.map(v => JsString(v.toString)))), None)
  }

  private[supler] override def applyFieldJSONValues(parentPath: FieldPath, obj: T, jsonFields: Map[String, JsValue]): PartiallyAppliedObj[T] = {
    import org.supler.validation.PartiallyAppliedObj._

    val values = for {
      jsonValue <- jsonFields.get(name).toList
      ids <- jsonValue match { case JsArray(ids) => List(ids.collect { case JsString(id) => id }); case _ => Nil }
      value <- ids
    } yield value.asInstanceOf[U]

    full(write(obj, values.toList))
  }
}

//class AlmostEditManyField[T, U](
//    name: String,
//    read: T => List[U],
//    write: (T, List[U]) => T,
//    labelForValue: U => String,
//    renderHint: Option[RenderHint with EditManyFieldCompatible]) {
//
//  def possibleValues(valuesProvider: ValuesProvider[T, U]): EditManyField[T, U] =
//    EditManyField(name, read, write, Nil, valuesProvider, None, labelForValue, None, renderHint,
//      AlwaysCondition, AlwaysCondition)
//}