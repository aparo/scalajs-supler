/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.field

import org.supler._
import org.supler.validation._
import play.api.libs.json._

case class SelectManyField[T, U](
  name: String,
  read: T => Set[U],
  write: (T, Set[U]) => T,
  validators: List[Validator[T, Set[U]]],
  valuesProvider: ValuesProvider[T, U],
  label: Option[String],
  labelForValue: U => String,
  description: Option[String],
  idForValue: Option[U => String],
  renderHint: Option[RenderHint with SelectManyFieldCompatible],
  enabledIf: T => Boolean,
  includeIf: T => Boolean) extends Field[T] with SelectField[T, U] with ValidateWithValidators[T, Set[U]] {

  def label(newLabel: String): SelectManyField[T, U] = this.copy(label = Some(newLabel))
  def description(newDescription: String): SelectManyField[T, U] = this.copy(description = Some(newDescription))
  def validate(validators: Validator[T, Set[U]]*): SelectManyField[T, U] = this.copy(validators = this.validators ++ validators)
  def renderHint(newRenderHint: RenderHint with SelectManyFieldCompatible): SelectManyField[T, U] = this.copy(renderHint = Some(newRenderHint))

  def enabledIf(condition: T => Boolean): SelectManyField[T, U] = this.copy(enabledIf = condition)
  def includeIf(condition: T => Boolean): SelectManyField[T, U] = this.copy(includeIf = condition)

  def idForValue[I](idFn: U => I)(implicit idTransformer: SelectValueIdSerializer[I]): SelectManyField[T, U] =
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

    full(write(obj, values.toSet))
  }
}

class AlmostSelectManyField[T, U](
  name: String,
  read: T => Set[U],
  write: (T, Set[U]) => T,
  labelForValue: U => String,
  renderHint: Option[RenderHint with SelectManyFieldCompatible]) {

  def possibleValues(valuesProvider: ValuesProvider[T, U]): SelectManyField[T, U] =
    SelectManyField(name, read, write, Nil, valuesProvider, None, labelForValue, None, None, renderHint,
      AlwaysCondition, AlwaysCondition)
}

