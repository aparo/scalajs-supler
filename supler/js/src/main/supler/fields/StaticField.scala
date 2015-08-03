/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler.fields

import app.utils.IDGenerator
import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.vdom.prefix_<^._
import supler.{GlobalOptions, Field, RenderHint, Validation}

import scala.scalajs.js

//params to be defined
case class StaticValue(key:Option[String]=None, params:List[String]=Nil)


object StaticValue {

  def fromJson(json:js.Dictionary[Any]): StaticValue ={


    var field=new StaticValue()
    for((fname, value)<-json){
      fname match {
        case "key" =>
          field=field.copy(key = Some(value.asInstanceOf[String]))
        case "params" =>
          field=field.copy(params = value.asInstanceOf[js.Array[_]].map(_.asInstanceOf[String]).toList)
      }
    }
    field
  }

}

case class StaticField(name:String, label:String="",  path:String="", validate:Validation[String]=Validation[String](),
                       value:StaticValue=StaticValue(),
                       renderHint:Option[RenderHint]=None, enabled:Boolean=true,
                       onChange:(Field=>Unit), globalOptions: GlobalOptions) extends Field {
  val `type`=StaticField.NAME


  def render(parentRenderHint:Option[RenderHint])(implicit idGenerator: IDGenerator): TagMod = {
    val id = idGenerator.getId
    <.div( ^.className :="form-group",
      <.label(^.`for` := id, label),
      <.div( ^.className :="form-control-static", value.key.getOrElse("[EMPTY]").toString)
     // <div class="text-danger" id="id68"></div>
    )

  }
  def extractValue: Option[(String, js.Any)] = None

}


object StaticField {
  val NAME="static"

  def fromJson(json:js.Dictionary[Any], onChange:(Field=>Unit), globalOptions: GlobalOptions): StaticField ={


    var field=new StaticField(name="", onChange=onChange, globalOptions=globalOptions)
    for((fname, value)<-json){
      fname match {
        case "label" =>
          field=field.copy(label = value.asInstanceOf[String])
        case "name" =>
          field = field.copy(name = value.asInstanceOf[String])
        case "enabled" =>
          field=field.copy(enabled = value.asInstanceOf[Boolean])
        case "type" =>

        case "path" =>
          field=field.copy(path = value.asInstanceOf[String])
        case "value" =>
          field=field.copy(value = StaticValue.fromJson(value.asInstanceOf[js.Dictionary[Any]]) )
        case "validate" =>
          field=field.copy(validate = Validation.fromJson[String](value.asInstanceOf[js.Dictionary[Any]]))
        case "render_hint" =>
          field=field.copy(renderHint = Some(RenderHint.fromJson(value.asInstanceOf[js.Dictionary[Any]])))
      }
    }
    field
  }

}