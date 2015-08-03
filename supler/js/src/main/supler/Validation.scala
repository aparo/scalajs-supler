/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler

import scala.scalajs.js

case class Validation[T](required:Boolean=false,
                         gt:Option[T]=None, gte:Option[T]=None,
                         lt:Option[T]=None, lte:Option[T]=None)
object Validation {
  def fromJson[T](json:js.Dictionary[Any]): Validation[T] ={
    var field=new Validation[T]()
    for((fname, value)<-json){
      fname match {
        case "required" =>
          field=field.copy(required = value.asInstanceOf[Boolean])
        case "gt" =>
          field=field.copy(gt = Some(value.asInstanceOf[T]))
        case "gte"|"ge" =>
          field=field.copy(gte = Some(value.asInstanceOf[T]))
        case "lt" =>
          field=field.copy(lt = Some(value.asInstanceOf[T]))
        case "lte"|"le" =>
          field=field.copy(lte = Some(value.asInstanceOf[T]))
      }
    }
    field
  }

}
