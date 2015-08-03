/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.validation

import org.supler.FieldPath
import play.api.libs.json._

trait ValidationScope {
  /**
   * @param parentPath Path of the parent of the field for which validation is to be checked.
   */
  def shouldValidate(parentPath: FieldPath, valueMissing: Boolean): Boolean
  def generateJSONData: ValidationScopeJSONData
}

case class ValidationScopeJSONData(name: String, extra: List[(String, JsValue)] = Nil)

object ValidateFilled extends ValidationScope {
  override def shouldValidate(parentPath: FieldPath, valueMissing: Boolean) = !valueMissing
  override def generateJSONData = ValidationScopeJSONData("filled")
}
object ValidateAll extends ValidationScope {
  override def shouldValidate(parentPath: FieldPath, valueMissing: Boolean) = true
  override def generateJSONData = ValidationScopeJSONData("all")
}
object ValidateNone extends ValidationScope {
  override def shouldValidate(parentPath: FieldPath, valueMissing: Boolean) = false
  override def generateJSONData = ValidationScopeJSONData("none")
}
case class ValidateInPath(rootPath: FieldPath) extends ValidationScope {
  override def shouldValidate(parentPath: FieldPath, valueMissing: Boolean) = parentPath.childOf(rootPath)
  override def generateJSONData = ValidationScopeJSONData("path", List("path" -> JsString(rootPath.toString)))
}
