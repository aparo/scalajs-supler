/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler

import upickle.Js

import scala.scalajs.js

case class PossibleValue(id:String, label:String)

object PossibleValue {
  def fromJson(json:js.Dictionary[Any]): PossibleValue ={
    var field=new PossibleValue("0", "")
    for((fname, value)<-json){
      fname match {
        case "id" =>
          field=field.copy(id = value.asInstanceOf[String])
        case "label" =>
          field=field.copy(label = value.asInstanceOf[String])
      }
    }
    field
  }
}