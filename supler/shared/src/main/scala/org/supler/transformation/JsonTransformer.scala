/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.transformation

import play.api.libs.json._


trait JsonTransformer[U] {
  /**
   * The name of the type that will be used in the generated json. Used by frontend to determine how to render the
   * field and serialize the value back to json.
   */
  def typeName: String
  def toJValueOrJNull(value: U): Option[JsValue] = {
    if (value == null) Some(JsNull)
    else toJValue(value)
  }
  def toJValue(value: U): Option[JsValue]
  def fromJValue(jvalue: JsValue): Option[U]
}

object JsonTransformer {
  trait JsonTransformerPF[U] extends JsonTransformer[U] {
    def fromJValue(jvalue: JsValue): Option[U] = fromJValuePF.lift(jvalue)

    def fromJValuePF: PartialFunction[JsValue, U]
  }

  implicit object JsValueJsonTransformer extends JsonTransformerPF[JsValue] {
    val typeName = "string"
    def toJValue(value: JsValue) = Some(value)
    def fromJValuePF = { case default:JsValue => default }
  }


  implicit object StringJsonTransformer extends JsonTransformerPF[String] {
    val typeName = "string"
    def toJValue(value: String) = Some(JsString(value))
    def fromJValuePF = { case JsString(v) => v }
  }

  implicit object IntJsonTransformer extends JsonTransformerPF[Int] {
    val typeName = "integer"
    def toJValue(value: Int) = Some(JsNumber(value))
    def fromJValuePF = { case JsNumber(v) => v.intValue() }
  }

  implicit object LongJsonTransformer extends JsonTransformerPF[Long] {
    val typeName = "integer"
    def toJValue(value: Long) = Some(JsNumber(value))
    def fromJValuePF = { case JsNumber(v) => v.longValue() }
  }

  implicit object FloatJsonTransformer extends JsonTransformerPF[Float] {
    val typeName = "float"
    def toJValue(value: Float) = Some(JsNumber(value.toDouble))
    def fromJValuePF = {
      case JsNumber(v) => v.toFloat
    }
  }

  implicit object DoubleJsonTransformer extends JsonTransformerPF[Double] {
    val typeName = "float"
    def toJValue(value: Double) = Some(JsNumber(value))
    def fromJValuePF = {
      case JsNumber(v) => v.toDouble
    }
  }

  implicit object BooleanJsonTransformer extends JsonTransformerPF[Boolean] {
    val typeName = "boolean"
    def toJValue(value: Boolean) = Some(JsBoolean(value))
    def fromJValuePF = { case JsBoolean(v) => v }
  }

  implicit def optionJsonTransformer[U](implicit inner: JsonTransformer[U]): JsonTransformer[Option[U]] =
    new JsonTransformer[Option[U]] {
      val typeName = inner.typeName
      def toJValue(value: Option[U]) = value.flatMap(inner.toJValueOrJNull)

      def fromJValue(jvalue: JsValue) = jvalue match {
        case JsNull => Some(None)
        case JsString("") => Some(None)
        case jv => inner.fromJValue(jv).map(Some(_))
      }
    }
}