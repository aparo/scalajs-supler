/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.validation

import org.supler.Message
import play.api.libs.json.{JsValue, JsNumber}

trait Validator[T, U] {
  def doValidate(fieldValue: U, objValue: T): List[Message]
  def generateJSON: List[(String, JsValue)]
}

trait Validators {
  def minLength[T](minLength: Int) =
    fieldValidator[T, String](_.length >= minLength)(_ => Message("error_length_tooShort", minLength))(Some("min_length", JsNumber(minLength)))

  def maxLength[T](maxLength: Int) =
    fieldValidator[T, String](_.length <= maxLength)(_ => Message("error_length_tooLong", maxLength))(Some("max_length", JsNumber(maxLength)))

  def gt[T, U](than: U)(implicit num: Numeric[U]) =
    fieldValidator[T, U](num.gt(_, than))(_ => Message("error_number_gt", than))(
      Some("gt", JsNumber(num.toDouble(than))))

  def lt[T, U](than: U)(implicit num: Numeric[U]) =
    fieldValidator[T, U](num.lt(_, than))(_ => Message("error_number_lt", than))(
      Some("lt", JsNumber(num.toDouble(than))))

  def ge[T, U](than: U)(implicit num: Numeric[U]) =
    fieldValidator[T, U](num.gteq(_, than))(_ => Message("error_number_ge", than))(
      Some("ge", JsNumber(num.toDouble(than))))

  def le[T, U](than: U)(implicit num: Numeric[U]) =
    fieldValidator[T, U](num.lteq(_, than))(_ => Message("error_number_le", than))(
      Some("le", JsNumber(num.toDouble(than))))

  def ifDefined[T, U](vs: Validator[T, U]*): Validator[T, Option[U]] =
    new Validator[T, Option[U]] {
      override def doValidate(fieldValue: Option[U], objValue: T) =
        fieldValue.map(fv => vs.toList.flatMap(_.doValidate(fv, objValue))).getOrElse(Nil)
      override def generateJSON = vs.flatMap(_.generateJSON).toList
    }

  def custom[T, U](validityTest: (U, T) => Boolean, createError: (U, T) => Message): Validator[T, U] = new Validator[T, U] {
    override def doValidate(fieldValue: U, objValue: T) = {
      if (!validityTest(fieldValue, objValue)) {
        List(createError(fieldValue, objValue))
      } else {
        Nil
      }
    }
    override def generateJSON = Nil
  }

  private def fieldValidator[T, U](validityTest: U => Boolean)(createError: U => Message)(json: Some[(String, JsValue)]) =
    new Validator[T, U] {
      override def doValidate(fieldValue: U, objValue: T) = {
        if (!validityTest(fieldValue)) {
          List(createError(fieldValue))
        } else {
          Nil
        }
      }

      override def generateJSON = json.toList
    }
}
