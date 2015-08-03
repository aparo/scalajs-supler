/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler.fields

import app.utils.IDGenerator
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import supler._

import scala.scalajs.js
import scala.util.Try


case class FloatField(name: String, onChange: (Field => Unit), label: String = "", validate: Validation[Float] = Validation[Float](), path: String = "",
                      var value: Option[Float] = None, emptyValue: Option[Float] = None,
                      renderHint: Option[RenderHint] = None, enabled: Boolean = true, globalOptions: GlobalOptions = GlobalOptions.default) extends Field {
  val `type` = FloatField.NAME

  def tryToFloat(s: String) = Try(s.toFloat).toOption

  def updateValue(e: ReactEventI) = {
    // update TodoItem content
    this.value = tryToFloat(e.currentTarget.value)
    onChange(this)
  }

  def extractValue: Option[(String, js.Any)] = this.value match {
    case Some(v) => Some(name -> v)
    case _ => None
  }

  def render(parentRenderHint: Option[RenderHint])(implicit idGenerator: IDGenerator): TagMod = {
    val id = idGenerator.getId
    var properties = List(^.`type` := "number", ^.id := id, ^.name := this.name,
      ^.className := "form-control", ^.onChange ==> this.updateValue)
    value match {
      case Some(vAl) =>
        properties ::= (^.value := vAl)
      case None =>
        if (emptyValue.isDefined)
          properties ::= (^.value := emptyValue.get)
    }

    <.div(^.className := "form-group" + this.getErrorClass,
      renderInputBody(id, parentRenderHint,
        <.input(properties: _*)
      )
    )

    //        <div class="form-group"><label for="id5">Age</label>
    //          <input type="number" value="10" id="id5" name="age" supler:fieldname="age" supler:fieldtype="integer" supler:validationid="id6" supler:path="age" class="form-control">
    //            <div class="text-danger" id="id6"></div>
    //          </div>
  }
}


object FloatField {
  val NAME = "float"

  def fromJson(json: js.Dictionary[Any], onChange: (Field => Unit), globalOptions: GlobalOptions): FloatField = {


    var field = new FloatField(name = "", onChange = onChange, globalOptions = globalOptions)
    for ((fname, value) <- json) {
      fname match {
        case "label" =>
          field = field.copy(label = value.asInstanceOf[String])
        case "name" =>
          field = field.copy(name = value.asInstanceOf[String])
        case "type" =>

        case "path" =>
          field = field.copy(path = value.asInstanceOf[String])
        case "enabled" =>
          field = field.copy(enabled = value.asInstanceOf[Boolean])
        case "value" =>
          field = field.copy(value = Some(value.asInstanceOf[Float]))
        case "empty_value" =>
          field = field.copy(emptyValue = Some(value.asInstanceOf[Float]))
        case "validate" =>
          field = field.copy(validate = Validation.fromJson[Float](value.asInstanceOf[js.Dictionary[Any]]))
        case "render_hint" =>
          field = field.copy(renderHint = Some(RenderHint.fromJson(value.asInstanceOf[js.Dictionary[Any]])))
      }
    }
    field
  }

}