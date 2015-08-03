/* Copyright 2009-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package org.supler.transformation

import java.text.{ParseException, SimpleDateFormat}
import java.util.{Date, UUID}

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.supler.Supler
import org.supler.field.{BasicFieldCompatible, RenderHint}
import play.api.libs.json.{ Json, JsObject }

trait BasicTypeTransformer[U, S] {
  def serialize(u: U): S
  def deserialize(s: S): Either[String, U]
  def renderHint: Option[RenderHint with BasicFieldCompatible] = None
}

// convenience traits for extension by custom transformers; one for each of the types which have a json transformer
trait StringTransformer[U] extends BasicTypeTransformer[U, String]
trait IntTransformer[U] extends BasicTypeTransformer[U, Int]
trait LongTransformer[U] extends BasicTypeTransformer[U, Long]
trait FloatTransformer[U] extends BasicTypeTransformer[U, Float]
trait DoubleTransformer[U] extends BasicTypeTransformer[U, Double]
trait BooleanTransformer[U] extends BasicTypeTransformer[U, Boolean]

object BasicTypeTransformer {
  trait IdentityTransformer[U] extends BasicTypeTransformer[U, U] {
    override def serialize(u: U) = u
    override def deserialize(u: U) = Right(u)
  }

  // if the type has a corresponding json transformer, no need to transform further
  implicit object StringIdTransformer extends IdentityTransformer[String]
  implicit object IntIdTransformer extends IdentityTransformer[Int]
  implicit object LongIdTransformer extends IdentityTransformer[Long]
  implicit object FloatIdTransformer extends IdentityTransformer[Float]
  implicit object DoubleIdTransformer extends IdentityTransformer[Double]
  implicit object BooleanIdTransformer extends IdentityTransformer[Boolean]

  implicit object UUIDTransformer extends StringTransformer[UUID] {
    override def serialize(u: UUID) = u.toString

    override def deserialize(s: String) = try {
      Right(UUID.fromString(s))
    } catch {
      case e: IllegalArgumentException => Left("error_illegalUUIDformat")
    }
  }

  val ISODateFormat = new SimpleDateFormat("yyyy-MM-dd")

  implicit object DateTransformer extends StringTransformer[Date] {
    override def serialize(d: Date) = ISODateFormat.format(d)

    override def deserialize(d: String) = try {
      Right(ISODateFormat.parse(d))
    } catch {
      case e: ParseException => Left("error_illegalDateformat")
    }

    override def renderHint = Some(Supler.asDate())
  }

  implicit val dateTimeTransformer = new StringTransformer[DateTime] {
    override def serialize(t: DateTime) = ISODateTimeFormat.date().print(t)

    override def deserialize(u: String) = try {
      Right(ISODateTimeFormat.date().parseDateTime(u))
    } catch {
      case e: IllegalArgumentException => Left("error_custom_illegalDateFormat")
    }
  }

  implicit val jsObjectTransformer = new StringTransformer[JsObject] {
    override def serialize(t: JsObject) = Json.stringify(t)

    override def deserialize(u: String) = try {
      Right(Json.parse(u).asInstanceOf[JsObject])
    } catch {
      case e: IllegalArgumentException => Left("error_custom_illegalJSONFormat")
    }
  }

  implicit def optionTransformer[U, S](implicit base: BasicTypeTransformer[U, S]): BasicTypeTransformer[Option[U], Option[S]] =
    new BasicTypeTransformer[Option[U], Option[S]] {
      override def serialize(u: Option[U]) = u.map(base.serialize)
      override def deserialize(s: Option[S]) = s.map(base.deserialize) match {
        case None => Right(None)
        case Some(d) => d.right.map(Some(_))
      }
      override def renderHint = base.renderHint
    }
}
