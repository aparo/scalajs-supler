/* Copyright 2014-2015 - Big Data Technologies S.R.L.  All Rights Reserved. */
package supler.fields


import app.utils.IDGenerator
import japgolly.scalajs.react._
import supler.{GlobalOptions, Error, RenderHint, Field}
import japgolly.scalajs.react.vdom.prefix_<^._
import web._
import scala.scalajs.js

case class ValidationScope(name: String = "")

object ValidationScope {

  def fromJson(json: js.Dictionary[Any]): ValidationScope = {

    var field = new ValidationScope()
    for ((fname, value) <- json) {
      fname match {
        case "name" =>
          field = field.copy(name = value.asInstanceOf[String])
      }
    }
    field
  }

}

case class ActionField(name: String, onChange: (Field => Unit), label: String = "", icon: String = "",
                       validationScope: ValidationScope = ValidationScope(), path: String = "",
                       enabled: Boolean = true, var value: Boolean = false, globalOptions: GlobalOptions = GlobalOptions.default
                        ) extends Field {
  val `type` = ActionField.NAME

  def clicked(e: ReactEventI) = {
    this.value = true
    onChange(this)
  }

  def getIcon:ReactElement = {
    if(icon.isEmpty)
      <.i()
    else if(icon.startsWith("fa fa-"))
      <.i(^.cls := icon)
    else
      <.i(^.cls := "fa fa-"+icon)
  }

  def render(parentRenderHint: Option[RenderHint])(implicit idGenerator: IDGenerator): TagMod = {

    val id = idGenerator.getId
    var cls=buttonActionClass
    if(label=="Delete")
      cls=buttonDeleteClass

    <.div(^.className := "form-group", <.label(^.`for` := id),
      <.button(^.`type` := "button", ^.id := id, ^.name := this.name, // supler:fieldname="addcar" supler:fieldtype="action" supler:validationid="id42" supler:path="addcar"
        ^.className := cls, ^.onClick ==> clicked, getIcon, " "+label)
      //      <div class="text-danger" id="id42"></div>
    )
  }

  def extractValue: Option[(String, js.Any)] = {
    if (this.value)
      Some(name -> this.value)
    else
      None
  }
}


object ActionField {
  val NAME = "action"

  def fromJson(json: js.Dictionary[Any], onChange: (Field => Unit), globalOptions: GlobalOptions): ActionField = {

    var field = new ActionField(name = "", onChange = onChange, globalOptions = globalOptions)
    for ((fname, value) <- json) {
      fname match {
        case "name" =>
          field = field.copy(name = value.asInstanceOf[String])
        case "label" =>
          field = field.copy(label = value.asInstanceOf[String])
        case "icon" =>
          field = field.copy(icon = value.asInstanceOf[String])
        case "type" =>

        case "enabled" =>
          field = field.copy(enabled = value.asInstanceOf[Boolean])
        case "path" =>
          field = field.copy(path = value.asInstanceOf[String])
        case "validation_scope" =>
          field = field.copy(validationScope = ValidationScope.fromJson(value.asInstanceOf[js.Dictionary[Any]]))
      }
    }
    field
  }

}