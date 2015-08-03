/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler

import scala.scalajs.js


case class RenderHint(name:String, rows:Option[Int]=None, cols:Option[Int]=None, collapsible:Option[Boolean]=None)

object RenderHint {
  def fromJson(json:js.Dictionary[Any]): RenderHint ={
    var field=new RenderHint("")
    for((fname, value)<-json){
      fname match {
        case "name" =>
          field=field.copy(name = value.asInstanceOf[String])
        case "rows" =>
          field=field.copy(rows = Some(value.asInstanceOf[Int]))
        case "cols" =>
          field=field.copy(cols = Some(value.asInstanceOf[Int]))
        case "collapsible" =>
          field=field.copy(collapsible = Some(value.asInstanceOf[Boolean]))
      }
    }
    field
  }

}
