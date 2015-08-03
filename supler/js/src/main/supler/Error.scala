/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler

import scala.scalajs.js

case class Error(fieldPath:String, errorKey:String, params:Array[Any]=Array()) {}

object Error{
  def fromJson(json:js.Dictionary[Any]):Error={
    var error=Error("", "")
    for((fname, value)<-json){
      fname match {
        case "field_path" =>
          error=error.copy(fieldPath = value.asInstanceOf[String])
        case "error_key" =>
          error=error.copy(errorKey = value.asInstanceOf[String])
        case "error_params" =>
//          error=error.copy(params = value.asInstanceOf[Array[Any]])
      }
    }
    error
  }
}