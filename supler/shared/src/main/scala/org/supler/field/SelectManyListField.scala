/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.field

import org.supler._
import org.supler.validation._
import play.api.libs.json._

case class SelectManyListField[T, U](
  name: String,
  read: T => List[U],
  write: (T, List[U]) => T,
  validators: List[Validator[T, List[U]]],
  valuesProvider: ValuesProvider[T, U],
  label: Option[String],
  labelForValue: U => String,
  description: Option[String],
  idForValue: Option[U => String],
  renderHint: Option[RenderHint with SelectManyFieldCompatible],
  enabledIf: T => Boolean,
  includeIf: T => Boolean) extends Field[T] with SelectField[T, U] with ValidateWithValidators[T, List[U]] {

  def label(newLabel: String): SelectManyListField[T, U] = this.copy(label = Some(newLabel))
  def description(newDescription: String): SelectManyListField[T, U] = this.copy(description = Some(newDescription))
  def validate(validators: Validator[T, List[U]]*): SelectManyListField[T, U] = this.copy(validators = this.validators ++ validators)
  def renderHint(newRenderHint: RenderHint with SelectManyFieldCompatible): SelectManyListField[T, U] = this.copy(renderHint = Some(newRenderHint))

  def enabledIf(condition: T => Boolean): SelectManyListField[T, U] = this.copy(enabledIf = condition)
  def includeIf(condition: T => Boolean): SelectManyListField[T, U] = this.copy(includeIf = condition)

  def idForValue[I](idFn: U => I)(implicit idTransformer: SelectValueIdSerializer[I]): SelectManyListField[T, U] =
    this.copy(idForValue = Some(idFn andThen idTransformer.toString))

  override def emptyValue = None
  override def required = false

  override protected def multiple = true

  protected def generateValueJSONData(obj: T) = {
    val possibleValues = valuesProvider(obj)
    val currentValues = read(obj)

    ValueJSONData(Some(JsArray(currentValues.toList.flatMap(idFromValue(possibleValues, _)).map(JsString))),
      None)
  }

  private[supler] override def applyFieldJSONValues(parentPath: FieldPath, obj: T, jsonFields: Map[String, JsValue]): PartiallyAppliedObj[T] = {
    import org.supler.validation.PartiallyAppliedObj._

    val possibleValues = valuesProvider(obj)
    val values = for {
      jsonValue <- jsonFields.get(name).toList
      ids <- jsonValue match { case JsArray(ids) => List(ids.collect { case JsString(id) => id }); case _ => Nil }
      id <- ids
      value <- valueFromId(possibleValues, id)
    } yield value

    full(write(obj, values.toList))
  }
}

class AlmostSelectManyListField[T, U](
  name: String,
  read: T => List[U],
  write: (T, List[U]) => T,
  labelForValue: U => String,
  renderHint: Option[RenderHint with SelectManyFieldCompatible]) {

  def possibleValues(valuesProvider: ValuesProvider[T, U]): SelectManyListField[T, U] =
    SelectManyListField(name, read, write, Nil, valuesProvider, None, labelForValue, None, None, renderHint,
      AlwaysCondition, AlwaysCondition)
}

