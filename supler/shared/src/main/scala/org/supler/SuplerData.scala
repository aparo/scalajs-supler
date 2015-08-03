/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler

import org.supler.field._
import org.supler.validation._
import play.api.libs.json._

/**
 * At any stage of processing data with Supler, the result can be serialized to JSON and sent to the server.
 * The data can be either:
 * - form with an object, and optional errors and custom data
 * - custom data only, represented as json. This can be a result of running an action.
 */
sealed trait SuplerData[+T] {
  def generateJSON: JsObject
}

trait FormWithObject[T] extends SuplerData[T] {
  def obj: T
  def form: Form[T]

  def id = form.id

  def options = form.options
  def meta: FormMeta
  /**
   * Custom data which will be included in the generated JSON, passed to the frontend.
   * Not used or manipulated in any other way by Supler.
   */
  def customData: Option[JsValue]

  protected def applyErrors: FieldErrors
  protected def validationErrors: FieldErrors
  protected def allErrors: FieldErrors = applyErrors ++ validationErrors

  def withMeta(key: String, value: String): FormWithObject[T]

  def applyJSONValues(jvalue: JsValue): FormWithObjectAndErrors[T] = {
    val result = form.applyJSONValues(EmptyPath, obj, jvalue)

    new FormWithObjectAndErrors(form, result.obj, customData, result.errors, Nil, FormMeta.fromJSON(jvalue))
  }

  def doValidate(scope: ValidationScope = ValidateAll): FormWithObjectAndErrors[T] = {
    val currentApplyErrors = applyErrors
    val newValidationErrors = form.doValidate(EmptyPath, obj, scope)

    new FormWithObjectAndErrors(form, obj, customData, currentApplyErrors, newValidationErrors, meta)
  }

  override def generateJSON: JsObject = {
    JsObject(Seq(meta.toJSON) ++
    List(
      "is_supler_form" -> JsBoolean(true),
      "main_form" -> form.generateJSON(EmptyPath, obj),
      "errors" -> JsArray(allErrors.map(_.generateJSON)),
      "custom_data" -> customData.getOrElse(JsObject(Nil)))
    )
  }

  /**
   * Processes a form basing on new field values and an optional action call, preparing a result that can be sent
   * back to the frontend.
   *
   * If there's no action, validation of filled fields is run and the result is returned.
   * If there's an action, the action result (optional) together with the new form (optional) is returned.
   */
  def process(jvalue: JsValue): SuplerData[T] = {
    val applied = this.applyJSONValues(jvalue)

    applied.findAndRunAction(jvalue) match {
      case Some(actionResult) => actionResult
      case None => applied.doValidate(ValidateFilled)
    }
  }

  /**
   * Finds an action, and if there's one, runs the required validation and if there are no errors, the action itself.
   * @return `Some` if an action was found. Contains the validated form with errors or the action result.
   */
  def findAndRunAction(jvalue: JsValue): Option[SuplerData[T]] = {
    form.findAction(EmptyPath, obj, jvalue, RunActionContext(Nil)).map { runnableAction =>
      val validated = this.doValidate(runnableAction.validationScope)
      if (validated.hasErrors) {
        validated
      } else {
        runnableAction.run() match {
          case FullCompleteActionResult(t, customData) => InitialFormWithObject(form, t.asInstanceOf[T], customData, meta)
          case CustomDataCompleteActionResult(json) => CustomDataOnly(json)
        }
      }
    }
  }
}

case class InitialFormWithObject[T](form: Form[T], obj: T, customData: Option[JsValue], meta: FormMeta)
  extends FormWithObject[T] {
  override protected val applyErrors = Nil
  override protected val validationErrors = Nil

  override def withMeta(key: String, value: String): InitialFormWithObject[T] = this.copy(meta = meta + (key, value))
}

case class FormWithObjectAndErrors[T](
  form: Form[T],
  obj: T,
  customData: Option[JsValue],
  applyErrors: FieldErrors,
  validationErrors: FieldErrors,
  meta: FormMeta) extends FormWithObject[T] {

  def errors: FieldErrors = allErrors
  def hasErrors: Boolean = allErrors.size > 0

  override def withMeta(key: String, value: String): FormWithObjectAndErrors[T] = this.copy(meta = meta + (key, value))
}

case class CustomDataOnly private[supler] (customData: JsObject) extends SuplerData[Nothing] {
  override def generateJSON = customData
}
