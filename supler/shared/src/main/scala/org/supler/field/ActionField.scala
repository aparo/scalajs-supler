/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.field

import org.supler.FieldPath
import org.supler.validation._
import play.api.libs.json._

case class ActionField[T](
  name: String,
  action: T => ActionResult[T],
  label: Option[String],
  description: Option[String],
  actionValidationScope: ActionValidationScope,
  enabledIf: T => Boolean,
    includeIf: T => Boolean,
    icon: Option[String] = None) extends Field[T] {

  require(name.matches("\\w+"), "Action name must contain only word characters (letters, numbers, _)")

  def label(newLabel: String): ActionField[T] = this.copy(label = Some(newLabel))

  def description(newDescription: String): ActionField[T] = this.copy(description = Some(newDescription))

  def icon(icon: String): ActionField[T] = this.copy(icon = Some(icon))

  def validateNone(): ActionField[T] = this.copy(actionValidationScope = BeforeActionValidateNone)
  def validateAll(): ActionField[T] = this.copy(actionValidationScope = BeforeActionValidateAll)
  def validateSubform(): ActionField[T] = this.copy(actionValidationScope = BeforeActionValidateSubform)

  def enabledIf(condition: T => Boolean): ActionField[T] = this.copy(enabledIf = condition)
  def includeIf(condition: T => Boolean): ActionField[T] = this.copy(includeIf = condition)

  private[supler] override def generateFieldJSON(parentPath: FieldPath, obj: T) = {
    import JSONFieldNames._

    val validationScopeJSONData = actionValidationScope.toValidationScope(parentPath).generateJSONData
    val validationScopeJSON = JsObject(Seq("name" -> JsString(validationScopeJSONData.name)) ++ validationScopeJSONData.extra)

    Json.obj(
      Label -> JsString(label.getOrElse("")),
      Icon -> JsString(icon.getOrElse("")),
      Type -> JsString(SpecialFieldTypes.Action),
      Path -> JsString(parentPath.append(name).toString),
      "validation_scope" -> validationScopeJSON
    )
  }

  private[supler] override def applyFieldJSONValues(parentPath: FieldPath, obj: T, jsonFields: Map[String, JsValue]) =
    PartiallyAppliedObj.full(obj)

  private[supler] override def doValidate(parentPath: FieldPath, obj: T, scope: ValidationScope) = Nil

  private[supler] override def findAction(parentPath: FieldPath, obj: T, jsonFields: Map[String, JsValue], ctx: RunActionContext) = {
    if (jsonFields.get(name) == Some(JsBoolean(value = true))) {
      Some(RunnableAction(
        parentPath.append(name),
        actionValidationScope.toValidationScope(parentPath),
        () => action(obj).completeWith(ctx)
      ))
    } else {
      None
    }
  }
}

trait ActionResult[+U] {
  private[supler] def completeWith(ctx: RunActionContext): CompleteActionResult
}

object ActionResult {
  def apply[T](t: T, customData: Option[JsObject] = None): ActionResult[T] = FullResult(t, customData)

  def custom(data: JsObject): ActionResult[Nothing] = CustomDataResult(data)

  def parentAction[T, U](action: (T, Int, U) => ActionResult[T]): U => ActionResult[U] = { u =>
    new ActionResult[U] {
      private[supler] override def completeWith(ctx: RunActionContext) = {
        val (t, i, _, parentCtx) = ctx.pop[T, U]()
        action(t, i, u).completeWith(parentCtx)
      }
    }
  }
}

private[supler] case class FullResult[U](result: U, customData: Option[JsObject]) extends ActionResult[U] {
  private[supler] override def completeWith(ctx: RunActionContext): CompleteActionResult = {
    val lastResult = ctx.parentsStack.foldLeft[Any](result) { case (r, (_, _, parentUpdate)) =>
      parentUpdate.asInstanceOf[Any => Any](r)
    }

    FullCompleteActionResult(lastResult, customData)
  }
}

private[supler] case class CustomDataResult(data: JsObject) extends ActionResult[Nothing] {
  private[supler] override def completeWith(ctx: RunActionContext) = CustomDataCompleteActionResult(data)
}

private[supler] sealed trait CompleteActionResult
private[supler] case class CustomDataCompleteActionResult(json: JsObject) extends CompleteActionResult
private[supler] case class FullCompleteActionResult(value: Any, customData: Option[JsObject]) extends CompleteActionResult

private[supler] case class RunActionContext(parentsStack: List[(Any, Int, Function1[_, _])]) {
  def push[T, U](obj: T, i: Int, defaultUpdate: U => T): RunActionContext =
    RunActionContext((obj, i, defaultUpdate) :: parentsStack)

  def pop[T, U](): (T, Int, U => T, RunActionContext) = {
    val (obj, i, defaultUpdate) = parentsStack.head
    (obj.asInstanceOf[T], i, defaultUpdate.asInstanceOf[U => T], RunActionContext(parentsStack.tail))
  }
}

private[supler] case class RunnableAction(
  path: FieldPath,
  validationScope: ValidationScope,
  run: () => CompleteActionResult)

trait ActionValidationScope {
  /**
   * Convert this action validation scope to a validation scope.
   * @param parentPath Path to the parent of the action field.
   */
  def toValidationScope(parentPath: FieldPath): ValidationScope
}
object BeforeActionValidateNone extends ActionValidationScope {
  def toValidationScope(parentPath: FieldPath) = ValidateNone
}
object BeforeActionValidateAll extends ActionValidationScope {
  def toValidationScope(parentPath: FieldPath) = ValidateAll
}
object BeforeActionValidateSubform extends ActionValidationScope {
  // validating the subform means validating all fields under the parent of the action field
  def toValidationScope(parentPath: FieldPath) = ValidateInPath(parentPath)
}